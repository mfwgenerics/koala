package io.koalaql.h2

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

class H2Dialect(
    private val compatibilityMode: H2CompatibilityMode? = null
): SqlDialect {
    private val compiler = object : Compiler {
        override fun addLiteral(builder: ScopedSqlBuilder, value: Literal<*>?) {
            builder.output.addLiteral(value)

            if (value?.type == JsonData::class && value.value != null) {
                builder.output.addSql(" FORMAT JSON")
            }
        }

        override fun excluded(builder: ScopedSqlBuilder, reference: Reference<*>) {
            when (compatibilityMode) {
                H2CompatibilityMode.MYSQL -> {
                    builder.addSql("VALUES")
                    builder.parenthesize {
                        when (reference) {
                            is Column<*> -> builder.addIdentifier(reference.symbol)
                            else -> builder.compileReference(reference)
                        }
                    }
                }
                null -> builder.addError("Excluded[] is not supported by this dialect")
            }
        }

        override fun <T : Any> reference(builder: ScopedSqlBuilder, emitParens: Boolean, value: Reference<T>) =
            builder.compileReference(value)

        override fun aggregatable(builder: ScopedSqlBuilder, emitParens: Boolean, aggregatable: BuiltAggregatable) =
            builder.compileAggregatable(aggregatable)

        override fun window(builder: ScopedSqlBuilder, window: BuiltWindow) {
            builder.compileWindow(window)
        }

        override fun <T : Any> dataTypeForCast(builder: ScopedSqlBuilder, to: UnmappedDataType<T>) =
            builder.compileCastDataType(to)

        override fun subquery(builder: ScopedSqlBuilder, emitParens: Boolean, subquery: BuiltSubquery) =
            builder.compileSubqueryExpr(subquery)
    }

    override fun ddl(change: SchemaChange): List<CompiledSql> {
        val results = mutableListOf<CompiledSql>()

        change.tables.created.forEach { (_, table) ->
            val sql = CompiledSqlBuilder(IdentifierQuoteStyle.DOUBLE)

            ScopedSqlBuilder(
                sql,
                Scope(NameRegistry { "C${it + 1}" }),
                compiler
            ).compileCreateTable(table)

            results.add(sql.toSql())
        }

        return results
    }

    private fun ScopedSqlBuilder.compileDataType(type: UnmappedDataType<*>) {
        addSql(type.defaultRawSql())
    }

    private fun ScopedSqlBuilder.compileExpr(expr: Expr<*>, emitParens: Boolean) {
        compiler.compileExpr(this, expr, emitParens)
    }

    private fun ScopedSqlBuilder.compileDefaultExpr(expr: Expr<*>) {
        when (expr) {
            is Literal -> compiler.addLiteral(this, expr)
            is Column<*> -> addIdentifier(expr.symbol)
            else -> compileExpr(expr, true)
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

        addTableReference(table)
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
                addTableReference(baseRelation)
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
        compileExpr(assignment.expr, true)
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

    fun ScopedSqlBuilder.compile(dml: BuiltDml): CompiledSql? = compile(dml,
        compileQuery = { compileQuery(it) },
        compileStmt = { compileStmt(it) }
    )

    fun ScopedSqlBuilder.compileWindow(window: BuiltWindow) = compileWindow(window,
        compileExpr = { compileExpr(it, false) },
        compileOrderBy = { compileOrderBy(it) }
    )

    override fun compile(dml: BuiltDml): CompiledSql? {
        val sql = ScopedSqlBuilder(
            CompiledSqlBuilder(IdentifierQuoteStyle.DOUBLE),
            Scope(NameRegistry { "C${it + 1}" }),
            compiler
        )

        return sql.compile(dml)
    }
}