package io.koalaql.mysql

import io.koalaql.Assignment
import io.koalaql.ddl.*
import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.ColumnDefaultExpr
import io.koalaql.ddl.built.ColumnDefaultValue
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.*
import io.koalaql.dsl.alias
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.*
import io.koalaql.window.LabeledWindow
import io.koalaql.window.built.BuiltWindow
import kotlin.reflect.KClass

class MysqlDialect(): SqlDialect {
    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<(ScopedSqlBuilder) -> Unit>()

        change.tables.created.forEach { (_, table) ->
            results.add { sql ->
                sql.compileCreateTable(table)
            }
        }

        change.tables.altered.forEach { (_, table) ->
            table.columns.created.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" ADD COLUMN ")

                    sql.compileColumnDef(column)
                }
            }

            table.columns.altered.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" MODIFY COLUMN ")

                    sql.compileColumnDef(column.newColumn)
                }
            }

            table.columns.dropped.forEach { column ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" DROP COLUMN ")
                    sql.addIdentifier(column)
                }
            }

            table.indexes.created.forEach { (name, index) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" ADD ")

                    sql.compileIndexDef(name, index)
                }
            }

            table.indexes.dropped.forEach { name ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" DROP INDEX ")
                    sql.addIdentifier(name)
                }
            }
        }

        change.tables.dropped.forEach { table ->
            results.add { sql ->
                sql.addSql("DROP TABLE ")
                sql.addIdentifier(table)
            }
        }

        return results.map {
            ScopedSqlBuilder(
                SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS),
                Scope(NameRegistry { "column_$it" })
            ).also(it).toSql()
        }
    }

    private fun ScopedSqlBuilder.compileAssignment(assignment: Assignment<*>) {
        compileExpr(assignment.reference, false)
        addSql(" = ")
        compileExpr(assignment.expr)
    }

    private fun ScopedSqlBuilder.compileDdlExpr(expr: Expr<*>) {
        when (expr) {
            is Literal -> addLiteral(expr)
            is Column<*> -> addIdentifier(expr.symbol)
            else -> compileExpr(expr, true)
        }
    }

    private fun UnmappedDataType<*>.rawSql(): String = when (this) {
        TIMESTAMP -> "DATETIME"
        is DATETIME -> {
            val suffix = precision?.let { "($precision)" } ?: ""
            "DATETIME$suffix"
        }
        is TIME -> {
            val suffix = precision?.let { "($precision)" } ?: ""
            "TIME$suffix"
        }
        else -> defaultRawSql()
    }

    private fun ScopedSqlBuilder.compileDataType(type: UnmappedDataType<*>) {
        addSql(type.rawSql())
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
            compileDdlExpr(finalExpr)
        }
    }

    private fun ScopedSqlBuilder.compileIndexDef(name: String, def: BuiltIndexDef) {
        addSql(when (def.type) {
            IndexType.PRIMARY -> "PRIMARY KEY"
            IndexType.UNIQUE -> "UNIQUE KEY"
            IndexType.INDEX -> "INDEX"
        })

        addSql(" ")
        addIdentifier(name)
        parenthesize {
            prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDdlExpr(key)
            }
        }
    }

    private fun ScopedSqlBuilder.compileCreateTable(table: Table) {
        addSql("CREATE TABLE IF NOT EXISTS ")

        addSql(table.tableName)
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
                comma.next {
                    compileIndexDef(index.name, index.def)
                }
            }

            addSql("\n")
        }
    }

    private fun ScopedSqlBuilder.compileReference(name: Reference<*>) {
        resolveReference(name)
    }

    private fun ScopedSqlBuilder.compileOrderBy(ordinals: List<Ordinal<*>>) = compileOrderBy(ordinals) {
        compileExpr(it, false)
    }

    private fun ScopedSqlBuilder.compileAggregatable(aggregatable: BuiltAggregatable) {
        if (aggregatable.distinct == Distinctness.DISTINCT) addSql("DISTINCT ")

        compileExpr(aggregatable.expr, false)

        if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
    }

    private fun ScopedSqlBuilder.compileWindow(window: BuiltWindow) = compileWindow(
        window,
        compileExpr = { compileExpr(it, false) },
        compileOrderBy = { compileOrderBy(it) }
    )

    fun UnmappedDataType<*>.rawCastSql(): String = when (this) {
        TEXT -> "CHAR"
        BOOLEAN,
        TINYINT,
        SMALLINT,
        INTEGER,
        BIGINT -> "SIGNED"
        TINYINT.UNSIGNED,
        SMALLINT.UNSIGNED,
        INTEGER.UNSIGNED,
        BIGINT.UNSIGNED -> "UNSIGNED"
        is VARBINARY -> "BINARY"
        is VARCHAR -> "CHAR"
        else -> rawSql()
    }

    private fun ScopedSqlBuilder.compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
        when {
            expr is OperationExpr<*> && expr.type == OperationType.CURRENT_TIMESTAMP -> {
                check(expr.args.isEmpty())

                parenthesize(emitParens) {
                    addSql("UTC_TIMESTAMP")
                }
            }
            else -> compileExpr(expr, emitParens, Expressions(this))
        }
    }

    private inline fun <T> ScopedSqlBuilder.scopedIn(query: PopulatesScope, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withScope(query)

        return compilation.block()
    }

    // TODO remove this after WITH changes
    private inline fun <T> ScopedSqlBuilder.scopedCtesIn(query: BuiltQuery, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withCtes(query)

        return compilation.block()
    }

    private fun ScopedSqlBuilder.compileQuery(query: BuiltUnionOperandQuery, omitRow: Boolean = false): Boolean {
        return when (query) {
            is BuiltSelectQuery -> {
                scopedIn(query) {
                    compileSelect(query)
                }
                true
            }
            is BuiltValuesQuery -> compileValues(query, omitRow)
        }
    }

    private fun BuiltUnionOperandQuery.canOmitRowKeyword(): Boolean = when (this) {
        is BuiltSelectQuery -> false
        is BuiltValuesQuery -> true
    }

    private fun BuiltQuery.canOmitRowKeyword(): Boolean = head.canOmitRowKeyword()
        && unioned.isEmpty()
        && orderBy.isEmpty()
        && offset == 0
        && limit == null

    private fun ScopedSqlBuilder.compileQuery(query: BuiltQuery, forInsert: Boolean = false): Boolean {
        return scopedCtesIn(query) {
            compileFullQuery(
                query = query,
                compileWiths = { compileWiths(it) },
                compileSubquery = { compileQuery(it, forInsert && query.canOmitRowKeyword()) },
                compileOrderBy = {
                    scopedIn(query) {
                        compileOrderBy(it)
                    }
                }
            )
        }
    }

    private fun ScopedSqlBuilder.compileSubqueryExpr(subquery: BuiltQuery) {
        parenthesize {
            compileQuery(subquery)
        }
    }

    private fun ScopedSqlBuilder.compileValues(query: BuiltValuesQuery, omitRow: Boolean): Boolean {
        return compileValues(query,
            compileExpr = { compileExpr(it, false) }
        ) { columns, row ->
            if (!omitRow) addSql("ROW ")

            compileRow(columns, row) { compileExpr(it, false) }
        }
    }

    private fun ScopedSqlBuilder.compileRelation(relation: BuiltRelation) {
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
        addSql(scope[relation.computedAlias])

        explicitLabels?.let { labels ->
            parenthesize {
                prefix("", ", ").forEach(labels) {
                    addReference(it)
                }
            }
        }
    }


    private fun ScopedSqlBuilder.compileRelabels(labels: List<Reference<*>>) {
        parenthesize {
            prefix("", ", ").forEach(labels) {
                addReference(it)
            }
        }
    }

    private fun ScopedSqlBuilder.compileWiths(withable: BuiltWithable) = compileWiths(
        withable,
        compileCte = { addCte(it) },
        compileRelabels = { compileRelabels(it) },
        compileQuery = { compileQuery(it) }
    )

    private fun ScopedSqlBuilder.compileSelect(select: BuiltSelectQuery) {
        selectClause(select) { compileExpr(it, false) }

        if (select.body.relation.relation != EmptyRelation) addSql("\nFROM ")

        compileQueryBody(
            select.body,
            compileExpr = { compileExpr(it, false) },
            compileRelation = { compileRelation(it) },
            compileWindows = { windows -> compileWindows(windows) }
        )
    }

    private fun ScopedSqlBuilder.compileInsert(insert: BuiltInsert): Boolean = compileInsert(
        insert,
        compileInsertLine = { compileInsertLine(insert) },
        compileQuery = { compileQuery(it, true) },
        compileOnConflict = {
            val relvar = insert.unwrapTable()

            val sql = withColumns(relvar.columns, alias(relvar.tableName))

            sql.compileOnConflict(it) {
                sql.compileAssignment(it)
            }
        }
    )

    private fun ScopedSqlBuilder.compileUpdate(update: BuiltUpdate) = compileUpdate(update,
        compileWiths = { compileWiths(it) },
        compileRelation = { compileRelation(it) },
        compileAssignment = { compileAssignment(it) },
        compileExpr = { compileExpr(it, false) }
    )

    private fun ScopedSqlBuilder.compileWindows(windows: List<LabeledWindow>) =
        compileWindowClause(windows) {
            compileWindow(it)
        }

    private fun ScopedSqlBuilder.compileDelete(delete: BuiltDelete) = compileDelete(delete,
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

    private inner class Expressions(
        val sql: ScopedSqlBuilder
    ) : ExpressionCompiler {
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
            sql.addSql(to.rawCastSql())
        }

        override fun window(window: BuiltWindow) {
            sql.compileWindow(window)
        }

        override fun excluded(reference: Reference<*>) {
            sql.addSql("VALUES")
            sql.parenthesize {
                when (reference) {
                    is Column<*> -> sql.addIdentifier(reference.symbol)
                    else -> sql.compileReference(reference)
                }
            }
        }
    }

    override fun compile(dml: BuiltDml): SqlText? {
        val sql = ScopedSqlBuilder(
            SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS),
            Scope(NameRegistry { "column_$it" })
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