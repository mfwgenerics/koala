package io.koalaql.postgres

import io.koalaql.Assignment
import io.koalaql.ddl.*
import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.ColumnDefaultExpr
import io.koalaql.ddl.built.ColumnDefaultValue
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.*
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.*
import io.koalaql.window.*
import io.koalaql.window.built.BuiltWindow
import kotlin.reflect.KClass

private fun UnmappedDataType<*>.toRawSql(): String = when (this) {
    DOUBLE -> "DOUBLE PRECISION"
    else -> defaultRawSql()
}

class PostgresDialect: SqlDialect {
    private fun compileDefaultExpr(sql: SqlTextBuilder, expr: Expr<*>) {
        when (expr) {
            is Literal -> sql.addLiteral(expr)
            is Column<*> -> sql.addIdentifier(expr.symbol)
            else -> error("not implemented")
        }
    }

    private fun compileDataType(sql: SqlTextBuilder, type: UnmappedDataType<*>) {
        sql.addSql(type.toRawSql())
    }

    private fun compileSerialType(sql: SqlTextBuilder, type: UnmappedDataType<*>) {
        when (type) {
            SMALLINT -> sql.addSql("SMALLSERIAL")
            INTEGER -> sql.addSql("SERIAL")
            BIGINT -> sql.addSql("BIGSERIAL")
            else -> sql.addError("no serial type corresponds to $type")
        }
    }

    private fun compileIndexDef(sql: SqlTextBuilder, name: String, def: BuiltIndexDef) {
        sql.addSql("CONSTRAINT ")

        sql.addIdentifier(name)

        sql.addSql(when (def.type) {
            IndexType.PRIMARY -> " PRIMARY KEY"
            IndexType.UNIQUE -> " UNIQUE"
            IndexType.INDEX -> " INDEX"
        })

        sql.addSql(" ")
        sql.parenthesize {
            sql.prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDefaultExpr(sql, key)
            }
        }
    }

    private fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
        sql.addSql("CREATE TABLE IF NOT EXISTS ")

        sql.addIdentifier(table.tableName)
        sql.parenthesize {
            val comma = sql.prefix("\n", ",\n")

            comma.forEach(table.columns.includingUnused()) {
                val def = it.builtDef

                sql.addIdentifier(it.symbol)
                sql.addSql(" ")

                if (def.autoIncrement) {
                    compileSerialType(sql, def.columnType.dataType)
                } else {
                    compileDataType(sql, def.columnType.dataType)
                }

                if (def.notNull) sql.addSql(" NOT NULL")

                def.default?.let { default ->
                    @Suppress("unchecked_cast")
                    val finalExpr = when (default) {
                        is ColumnDefaultExpr -> default.expr
                        is ColumnDefaultValue -> Literal(
                            def.columnType.type as KClass<Any>,
                            default.value
                        )
                    }

                    sql.addSql(" DEFAULT ")
                    compileDefaultExpr(sql, finalExpr)
                }
            }

            table.primaryKey?.let { pk ->
                comma.next {
                    sql.addSql("CONSTRAINT ")
                    sql.addIdentifier(pk.name)
                    sql.addSql(" PRIMARY KEY (")
                    sql.prefix("", ", ").forEach(pk.def.keys.keys) {
                        when (it) {
                            is TableColumn<*> -> sql.addIdentifier(it.symbol)
                            else -> error("expression keys unsupported")
                        }
                    }
                    sql.addSql(")")
                }
            }

            table.indexes.forEach { index ->
                comma.next {
                    compileIndexDef(sql, index.name, index.def)
                }
            }

            sql.addSql("\n")
        }
    }

    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<SqlText>()

        change.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            compileCreateTable(sql, table)

            results.add(sql.toSql())
        }

        return results
    }


    fun ScopedSqlBuilder.compileReference(name: Reference<*>) {
        resolveReference(name)
    }

    fun ScopedSqlBuilder.compileOrderBy(ordinals: List<Ordinal<*>>) {
        compileOrderBy(ordinals) {
            compileExpr(it, false)
        }
    }

    fun ScopedSqlBuilder.compileAggregatable(aggregatable: BuiltAggregatable) {
        if (aggregatable.distinct == Distinctness.DISTINCT) addSql("DISTINCT ")

        compileExpr(aggregatable.expr, false)

        if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
    }

    fun ScopedSqlBuilder.compileWindow(window: BuiltWindow) =
        compileWindow(window,
            compileExpr = { compileExpr(it) },
            compileOrderBy = { compileOrderBy(it) }
        )

    fun ScopedSqlBuilder.compileCastDataType(type: UnmappedDataType<*>) {
        addSql(type.toRawSql())
    }

    fun ScopedSqlBuilder.compileQuery(query: BuiltQuery): Boolean {
        return scopedCtesIn(query) {
            compileFullQuery(
                query = query,
                compileWiths = { compileWiths(it) },
                compileSubquery = { compileQuery(it) },
                compileOrderBy = {
                    scopedIn(query) {
                        compileOrderBy(it)
                    }
                }
            )
        }
    }

    fun ScopedSqlBuilder.compileQuery(query: BuiltUnionOperandQuery): Boolean {
        val compilation = withScope(query)

        return when (query) {
            is BuiltSelectQuery -> {
                compilation.compileSelect(query)
                return true
            }
            is BuiltValuesQuery -> compilation.compileValues(query)
        }
    }

    fun ScopedSqlBuilder.compileSubqueryExpr(subquery: BuiltQuery) {
        parenthesize {
            compileQuery(subquery)
        }
    }

    fun ScopedSqlBuilder.compileSetLhs(expr: Reference<*>) {
        resolveWithoutAlias(expr)
    }

    fun ScopedSqlBuilder.compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
        compileExpr(expr, emitParens, Expressions(this))
    }

    fun ScopedSqlBuilder.compileRelation(relation: BuiltRelation) {
        val explicitLabels = when (val baseRelation = relation.relation) {
            is TableRelation -> {
                addIdentifier(baseRelation.tableName)
                null
            }
            is Subquery -> {
                parenthesize {
                    compileQuery(baseRelation.of)
                }

                if (baseRelation.of.columnsUnnamed()) {
                    baseRelation.of.columns
                } else {
                    null
                }
            }
            is Cte -> {
                addCte(baseRelation)

                if (relation.computedAlias.identifier == baseRelation.identifier) return

                null
            }
            is EmptyRelation -> return
        }

        addSql(" ")
        addAlias(relation)

        explicitLabels?.let { labels ->
            parenthesize {
                prefix("", ", ").forEach(labels) {
                    addReference(it)
                }
            }
        }
    }

    fun ScopedSqlBuilder.compileRelabels(labels: List<Reference<*>>) {
        parenthesize {
            prefix("", ", ").forEach(labels) {
                addReference(it)
            }
        }
    }

    fun ScopedSqlBuilder.compileWiths(withable: BuiltWithable) = compileWiths(
        withable,
        compileCte = { addCte(it) },
        compileRelabels = { compileRelabels(it) },
        compileQuery = { compileQuery(it) }
    )

    fun ScopedSqlBuilder.compileSelect(select: BuiltSelectQuery) {
        selectClause(select) { compileExpr(it, false) }

        if (select.body.relation.relation != EmptyRelation) addSql("\nFROM ")

        compileQueryBody(
            select.body,
            compileExpr = { compileExpr(it, false) },
            compileRelation = { compileRelation(it) },
            compileWindows = { windows -> compileWindows(windows) }
        )
    }

    fun ScopedSqlBuilder.compileValues(query: BuiltValuesQuery): Boolean {
        return compileValues(query, compileExpr = { compileExpr(it, false) })
    }

    fun ScopedSqlBuilder.compileAssignment(assignment: Assignment<*>) {
        compileSetLhs(assignment.reference)
        addSql(" = ")
        compileExpr(assignment.expr)
    }

    fun ScopedSqlBuilder.compileInsert(insert: BuiltInsert): Boolean {
        val relvar = insert.unwrapTable()

        compileInsertLine(insert) {
            addIdentifier(relvar.tableName)
            addSql(" AS ")
            addAlias(insert.relation)
        }

        addSql("\n")

        val nonEmpty = compileQuery(insert.query)

        val updateCtx = Expressions(withColumns(relvar.columns, insert.relation.computedAlias))

        compileOnConflict(insert.onConflict) { assignment ->
            updateCtx.sql.compileAssignment(assignment)
        }

        return nonEmpty
    }

    fun ScopedSqlBuilder.compileWindows(windows: List<LabeledWindow>) = compileWindowClause(windows) { window ->
        compileWindow(window)
    }

    fun ScopedSqlBuilder.compileDelete(delete: BuiltDelete) = compileDelete(delete,
        compileWiths = { compileWiths(it) },
        compileQueryBody = { query ->
            compileQueryBody(
                query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { compileWindows(it) }
            )
        }
    )

    private inline fun <T> ScopedSqlBuilder.scopedIn(query: PopulatesScope, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withScope(query)

        return compilation.block()
    }

    private inline fun <T> ScopedSqlBuilder.scopedCtesIn(query: BuiltQuery, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withCtes(query)

        return compilation.block()
    }

    fun ScopedSqlBuilder.compileUpdate(update: BuiltUpdate) = compileUpdate(update,
        compileWiths = { compileWiths(it) },
        compileRelation = { compileRelation(it) },
        compileAssignment = { compileAssignment(it) },
        compileExpr = { compileExpr(it, false) }
    )

    private inner class Expressions(
        val sql: ScopedSqlBuilder
    ): ExpressionCompiler {
        override fun excluded(reference: Reference<*>) {
            sql.addSql("EXCLUDED.")

            when (reference) {
                is Column<*> -> sql.addIdentifier(reference.symbol)
                else -> sql.compileReference(reference)
            }
        }

        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) {
            sql.compileReference(value)
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltQuery) {
            sql.compileSubqueryExpr(subquery)
        }

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) {
            sql.compileAggregatable(aggregatable)
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) {
            sql.compileCastDataType(to)
        }

        override fun window(window: BuiltWindow) {
            sql.compileWindow(window)
        }
    }

    override fun compile(dml: BuiltDml): SqlText? {
        val sql = ScopedSqlBuilder(
            SqlTextBuilder(IdentifierQuoteStyle.DOUBLE),
            Scope(NameRegistry { "column${it+1}" })
        )

        return sql.compile(dml,
            compileQuery = { sql.compileQuery(it) },
            compileInsert = { sql.scopedIn(it) { compileInsert(it) } },
            compileUpdate = { sql.scopedIn(it) { compileUpdate(it) } },
            compileDelete = {
                sql.scopedIn(dml) { compileDelete(it) }
                true
            }
        )
    }
}