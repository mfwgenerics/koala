package io.koalaql.h2

import io.koalaql.Assignment
import io.koalaql.ddl.IndexType
import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.UnmappedDataType
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

class H2Dialect(
    private val compatibilityMode: H2CompatibilityMode? = null
): SqlDialect {
    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<SqlText>()

        change.tables.created.forEach { (_, table) ->
            val sql = SqlTextBuilder(IdentifierQuoteStyle.DOUBLE)

            ScopedSqlBuilder(
                sql,
                Scope(NameRegistry { "C${it + 1}" })
            ).compileCreateTable(table)

            results.add(sql.toSql())
        }

        return results
    }

    private fun ScopedSqlBuilder.compileDataType(type: UnmappedDataType<*>) {
        addSql(type.defaultRawSql())
    }

    fun ScopedSqlBuilder.compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
        compileExpr(expr, emitParens, Expressions(this))
    }

    private fun ScopedSqlBuilder.compileDefaultExpr(expr: Expr<*>) {
        when (expr) {
            is Literal -> addLiteral(expr)
            is Column<*> -> addIdentifier(expr.symbol)
            else -> compileExpr(expr)
        }
    }

    private fun ScopedSqlBuilder.compileColumnDef(column: TableColumn<*>) {
        val def = column.builtDef

        addIdentifier(column.symbol)
        addSql(" ")
        compileDataType(def.columnType.dataType)

        if (def.autoIncrement) addSql(" AUTO_INCREMENT")
        if (def.notNull) addSql(" NOT NULL")

        def.default?.let { default ->
            @Suppress("unchecked_cast")
            val finalExpr = when (default) {
                is ColumnDefaultExpr -> default.expr
                is ColumnDefaultValue -> Literal(
                    def.columnType.type as KClass<Any>,
                    default.value
                )
            }

            addSql(" DEFAULT ")
            compileDefaultExpr(finalExpr)
        }
    }

    private fun ScopedSqlBuilder.compileUniqueDef(name: String, def: BuiltIndexDef) {
        addSql("CONSTRAINT ")
        addIdentifier(name)
        addSql(" UNIQUE")
        parenthesize {
            prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDefaultExpr(key)
            }
        }
    }

    fun ScopedSqlBuilder.compileCreateTable(table: Table) {
        addSql("CREATE TABLE IF NOT EXISTS ")

        addIdentifier(table.tableName)
        parenthesize {
            val comma = prefix("\n", ",\n")

            comma.forEach(table.columns.includingUnused()) {
                compileColumnDef(it)
            }

            table.primaryKey?.let { pk ->
                comma.next {
                    addSql("CONSTRAINT ")
                    addIdentifier(pk.name)
                    addSql(" PRIMARY KEY (")
                    prefix("", ", ").forEach(pk.def.keys.keys) {
                        when (it) {
                            is TableColumn<*> -> addIdentifier(it.symbol)
                            else -> error("expression keys unsupported")
                        }
                    }
                    addSql(")")
                }
            }

            table.indexes.forEach { index ->
                if (index.def.type != IndexType.INDEX) {
                    comma.next {
                        compileUniqueDef(index.name, index.def)
                    }
                }
            }

            addSql("\n")
        }
    }

    fun ScopedSqlBuilder.compileReference(name: Reference<*>) {
        resolveReference(name)
    }

    fun ScopedSqlBuilder.compileOrderBy(ordinals: List<Ordinal<*>>) =
        compileOrderBy(ordinals) { compileExpr(it, false) }

    fun ScopedSqlBuilder.compileAggregatable(aggregatable: BuiltAggregatable) {
        if (aggregatable.distinct == Distinctness.DISTINCT) addSql("DISTINCT ")

        compileExpr(aggregatable.expr, false)

        if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
    }

    fun ScopedSqlBuilder.compileCastDataType(type: UnmappedDataType<*>) {
        addSql(type.defaultRawSql())
    }

    private inline fun <T> ScopedSqlBuilder.scopedIn(query: PopulatesScope, block: ScopedSqlBuilder.() -> T): T {
        return withScope(query).block()
    }

    private inline fun <T> ScopedSqlBuilder.scopedCtesIn(query: BuiltQuery, block: ScopedSqlBuilder.() -> T): T {
        return withCtes(query).block()
    }

    fun ScopedSqlBuilder.compileQuery(query: BuiltUnionOperandQuery): Boolean {
        return when (query) {
            is BuiltSelectQuery -> {
                scopedIn(query) {
                    compileSelect(query)
                }
                true
            }
            is BuiltValuesQuery -> compileValues(query)
        }
    }

    fun ScopedSqlBuilder.compileStmt(stmt: BuiltStatement): Boolean =
        when (stmt) {
            is BuiltInsert -> { compileInsert(stmt) }
            is BuiltUpdate -> { compileUpdate(stmt) }
            is BuiltDelete -> {
                compileDelete(stmt)
                true
            }
        }

    fun ScopedSqlBuilder.compileQuery(query: BuiltSubquery): Boolean =
        when (query) {
            is BuiltQuery -> compileFullQuery(query)
            is BuiltReturning -> compileReturning(query,
                compileStmt = { compileStmt(it) },
                compileExpr = { compileExpr(it, false) }
            )
        }

    fun ScopedSqlBuilder.compileSubqueryExpr(subquery: BuiltSubquery) {
        parenthesize {
            compileQuery(subquery)
        }
    }

    fun ScopedSqlBuilder.compileFullQuery(query: BuiltQuery): Boolean {
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

    fun ScopedSqlBuilder.compileRelabels(labels: List<Reference<*>>) {
        parenthesize {
            prefix("", ", ").forEach(labels) {
                addReference(it)
            }
        }
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
        addAlias(relation.computedAlias)

        explicitLabels?.let { labels ->
            compileRelabels(labels)
        }
    }

    fun ScopedSqlBuilder.compileWiths(withable: BuiltWithable) = compileWiths(
        withable,
        compileCte = { addCte(it) },
        compileRelabels = { compileRelabels(it) },
        compileQuery = { compileQuery(it) }
    )

    fun ScopedSqlBuilder.compileWindowClause(windows: List<LabeledWindow>) =
        compileWindowClause(windows) { window ->
            compileWindow(window,
                compileExpr = { compileExpr(it, false) },
                compileOrderBy = { compileOrderBy(it) }
            )
        }

    fun ScopedSqlBuilder.compileSelect(select: BuiltSelectQuery) {
        selectClause(select) { compileExpr(it, false) }

        if (select.body.relation.relation != EmptyRelation) addSql("\nFROM ")

        compileQueryBody(
            select.body,
            compileExpr = { compileExpr(it, false) },
            compileRelation = { compileRelation(it) },
            compileWindows = { windows -> compileWindowClause(windows) }
        )
    }

    fun ScopedSqlBuilder.compileValues(query: BuiltValuesQuery): Boolean {
        return compileValues(query, compileExpr = { compileExpr(it, false) })
    }

    private fun ScopedSqlBuilder.compileAssignment(assignment: Assignment<*>) {
        compileExpr(assignment.reference, false)
        addSql(" = ")
        compileExpr(assignment.expr)
    }

    fun ScopedSqlBuilder.compileInsert(insert: BuiltInsert): Boolean = compileInsert(
        insert,
        compileInsertLine = { compileInsertLine(insert) },
        compileQuery = { compileQuery(it) },
        compileOnConflict = {
            val relvar = insert.unwrapTable()

            val sql = withColumns(relvar.columns)

            sql.compileOnConflict(it) {
                sql.compileAssignment(it)
            }
        }
    )

    fun ScopedSqlBuilder.compileUpdate(update: BuiltUpdate) = compileUpdate(update,
        compileWiths = { compileWiths(it) },
        compileRelation = { compileRelation(it) },
        compileAssignment = {
            compileAssignment(it)
        },
        compileExpr = { compileExpr(it, false) }
    )

    fun ScopedSqlBuilder.compileDelete(delete: BuiltDelete) = compileDelete(delete,
        compileWiths = { compileWiths(it) },
        compileQueryBody = { query ->
            compileQueryBody(
                query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindowClause(windows) }
            )
        }
    )

    fun ScopedSqlBuilder.compile(dml: BuiltDml): SqlText? = compile(dml,
        compileQuery = { compileQuery(it) },
        compileStmt = { compileStmt(it) }
    )

    fun ScopedSqlBuilder.compileWindow(window: BuiltWindow) = compileWindow(window,
        compileExpr = { compileExpr(it, false) },
        compileOrderBy = { compileOrderBy(it) }
    )

    private inner class Expressions(
        val sql: ScopedSqlBuilder
    ) : ExpressionCompiler {
        override fun excluded(reference: Reference<*>) {
            when (compatibilityMode) {
                H2CompatibilityMode.MYSQL -> {
                    sql.addSql("VALUES")
                    sql.parenthesize {
                        when (reference) {
                            is Column<*> -> sql.addIdentifier(reference.symbol)
                            else -> sql.compileReference(reference)
                        }
                    }
                }
                null -> sql.addError("Excluded[] is not supported by this dialect")
            }
        }

        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) =
            sql.compileReference(value)

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) =
            sql.compileAggregatable(aggregatable)

        override fun window(window: BuiltWindow) {
            sql.compileWindow(window)
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) =
            sql.compileCastDataType(to)

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) =
            sql.compileSubqueryExpr(subquery)
    }

    override fun compile(dml: BuiltDml): SqlText? {
        val sql = ScopedSqlBuilder(
            SqlTextBuilder(IdentifierQuoteStyle.DOUBLE),
            Scope(NameRegistry { "C${it + 1}" })
        )

        return sql.compile(dml)
    }
}