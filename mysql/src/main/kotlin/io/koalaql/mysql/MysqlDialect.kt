package io.koalaql.mysql

import io.koalaql.ddl.*
import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.ColumnDefaultExpr
import io.koalaql.ddl.built.ColumnDefaultValue
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.*
import io.koalaql.dsl.value
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.*
import io.koalaql.window.FrameRangeMarker
import io.koalaql.window.LabeledWindow
import io.koalaql.window.built.BuiltWindow
import kotlin.reflect.KClass

class MysqlDialect(): SqlDialect {
    override fun ddl(change: SchemaChange): List<SqlText> {
        val results = mutableListOf<(SqlTextBuilder) -> Unit>()

        change.tables.created.forEach { (_, table) ->
            results.add { sql ->
                Compilation(
                    scope = Scope(NameRegistry()),
                    sql = sql
                ).compileCreateTable(sql, table)
            }
        }

        change.tables.altered.forEach { (_, table) ->
            table.columns.created.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" ADD COLUMN ")

                    Compilation(
                        scope = Scope(NameRegistry()),
                        sql = sql
                    ).compileColumnDef(sql, column)
                }
            }

            table.columns.altered.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.tableName)
                    sql.addSql(" MODIFY COLUMN ")

                    Compilation(
                        scope = Scope(NameRegistry()),
                        sql = sql
                    ).compileColumnDef(sql, column.newColumn)
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

                    Compilation(
                        scope = Scope(NameRegistry()),
                        sql = sql
                    ).compileIndexDef(sql, name, index)
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
            SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS).also(it).toSql()
        }
    }

    private class Compilation(
        val scope: Scope,
        override val sql: SqlTextBuilder = SqlTextBuilder(IdentifierQuoteStyle.BACKTICKS)
    ): ExpressionCompiler {
        private fun compileDdlExpr(expr: Expr<*>) {
            when (expr) {
                is Literal -> sql.addLiteral(expr)
                is Column<*> -> sql.addIdentifier(expr.symbol)
                else -> compileExpr(expr, true)
            }
        }

        private fun UnmappedDataType<*>.rawSql(): String = when (this) {
            TIMESTAMP -> "DATETIME"
            is DATETIME -> {
                val suffix = precision?.let { "($precision)" }?:""
                "DATETIME$suffix"
            }
            is TIME -> {
                val suffix = precision?.let { "($precision)" }?:""
                "TIME$suffix"
            }
            else -> defaultRawSql()
        }

        private fun compileDataType(sql: SqlTextBuilder, type: UnmappedDataType<*>) {
            sql.addSql(type.rawSql())
        }

        fun compileColumnDef(sql: SqlTextBuilder, column: TableColumn<*>) {
            val def = column.builtDef

            sql.addIdentifier(column.symbol)
            sql.addSql(" ")
            compileDataType(sql, def.columnType.dataType)

            if (def.autoIncrement) sql.addSql(" AUTO_INCREMENT")
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
                compileDdlExpr(finalExpr)
            }
        }

        fun compileIndexDef(sql: SqlTextBuilder, name: String, def: BuiltIndexDef) {
            sql.addSql(when (def.type) {
                IndexType.PRIMARY -> "PRIMARY KEY"
                IndexType.UNIQUE -> "UNIQUE KEY"
                IndexType.INDEX -> "INDEX"
            })

            sql.addSql(" ")
            sql.addIdentifier(name)
            sql.parenthesize {
                sql.prefix("", ", ").forEach(def.keys.keys) { key ->
                    compileDdlExpr(key)
                }
            }
        }

        fun compileCreateTable(sql: SqlTextBuilder, table: Table) {
            sql.addSql("CREATE TABLE IF NOT EXISTS ")

            sql.addSql(table.tableName)
            sql.parenthesize {
                val comma = sql.prefix("\n", ",\n")

                comma.forEach(table.columns.includingUnused()) {
                    compileColumnDef(sql, it)
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

        override fun <T : Any> reference(emitParens: Boolean, value: Reference<T>) {
            compileReference(value)
        }

        override fun subquery(emitParens: Boolean, subquery: BuiltSubquery) {
            compileSubqueryExpr(subquery)
        }

        override fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable) {
            compileAggregatable(aggregatable)
        }

        override fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>) {
            sql.addSql(to.rawCastSql())
        }

        override fun window(window: BuiltWindow) {
            compileWindow(window)
        }

        fun compileReference(name: Reference<*>) {
            sql.withResult(scope.resolve(name)) {
                sql.addResolved(it)
            }
        }

        fun compileOrderBy(ordinals: List<Ordinal<*>>) = sql.compileOrderBy(ordinals) {
            compileExpr(it, false)
        }

        fun compileAggregatable(aggregatable: BuiltAggregatable) {
            if (aggregatable.distinct == Distinctness.DISTINCT) sql.addSql("DISTINCT ")

            compileExpr(aggregatable.expr, false)

            if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
        }

        fun compileRangeMarker(direction: String, marker: FrameRangeMarker<*>) =
            sql.compileRangeMarker(direction, marker) { compileExpr(it) }

        fun compileWindow(window: BuiltWindow) {
            val partitionedBy = window.partitions.partitions
            val orderBy = window.partitions.orderBy

            val prefix = sql.prefix("", " ")

            window.partitions.from?.let {
                prefix.next {
                    sql.addSql(scope.nameOf(it))
                }
            }

            if (partitionedBy.isNotEmpty()) prefix.next {
                sql.prefix("PARTITION BY ", ", ").forEach(partitionedBy) {
                    compileExpr(it, false)
                }
            }

            if (orderBy.isNotEmpty()) prefix.next {
                compileOrderBy(orderBy)
            }

            window.type?.let { windowType ->
                prefix.next {
                    sql.addSql(windowType.sql)
                    sql.addSql(" ")

                    val until = window.until

                    if (until == null) {
                        compileRangeMarker("PRECEDING", window.from)
                    } else {
                        sql.addSql("BETWEEN ")
                        compileRangeMarker("PRECEDING", window.from)
                        sql.addSql(" AND ")
                        compileRangeMarker("FOLLOWING", until)
                    }
                }
            }
        }

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

        fun compileQuery(outerSelect: List<SelectedExpr<*>>, query: BuiltSubquery, forInsert: Boolean): Boolean {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return when (query) {
                is BuiltSelectQuery -> {
                    compilation.compileSelect(outerSelect, query)
                    true
                }
                is BuiltValuesQuery -> compilation.compileValues(query, forInsert)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(emptyList(), subquery, false)
            }
        }

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            when {
                expr is OperationExpr<*> && expr.type == OperationType.CURRENT_TIMESTAMP -> {
                    check(expr.args.isEmpty())

                    sql.parenthesize(emitParens) {
                        sql.addSql("UTC_TIMESTAMP")
                    }
                }
                else -> sql.compileExpr(expr, emitParens, this)
            }
        }

        override fun excluded(reference: Reference<*>) {
            sql.addSql("VALUES")
            sql.parenthesize {
                compileReference(reference)
            }
        }

        fun compileRelation(relation: BuiltRelation) {
            val explicitLabels = when (val baseRelation = relation.relation) {
                is TableRelation -> {
                    sql.addSql(baseRelation.tableName)
                    null
                }
                is Subquery -> {
                    val innerScope = scope.innerScope()

                    baseRelation.of.populateScope(innerScope)

                    sql.parenthesize {
                        Compilation(
                            innerScope,
                            sql = sql
                        ).compileQuery(emptyList(), baseRelation.of, false)
                    }

                    if (baseRelation.of is BuiltValuesQuery) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
                }
                is Values -> {
                    sql.parenthesize {
                        compileValues(BuiltValuesQuery(baseRelation), false)
                    }
                    baseRelation.columns
                }
                is Cte -> {
                    sql.addSql(scope[baseRelation])

                    if (relation.computedAlias.identifier == baseRelation.identifier) return

                    null
                }
                is EmptyRelation -> return
            }

            sql.addSql(" ")
            sql.addSql(scope[relation.computedAlias])

            explicitLabels?.let { labels ->
                sql.parenthesize {
                    sql.prefix("", ", ").forEach(labels) {
                        sql.addSql(scope.nameOf(it))
                    }
                }
            }
        }

        fun compileSetOperation(
            outerSelect: List<SelectedExpr<*>>,
            operation: BuiltSetOperation
        ) {
            sql.addSql("\n")
            sql.addSql(operation.type.sql)
            if (operation.distinctness == Distinctness.ALL) sql.addSql(" ALL")
            sql.addSql("\n")

            val selectQuery = operation.body.toSelectQuery(outerSelect)

            Compilation(
                scope.innerScope().also {
                    selectQuery.populateScope(it)
                },
                sql
            ).compileSelect(outerSelect, selectQuery)
        }

        fun compileRelabels(labels: List<Reference<*>>) {
            sql.parenthesize {
                sql.prefix("", ", ").forEach(labels) {
                    sql.addIdentifier(scope.nameOf(it))
                }
            }
        }

        fun compileWiths(withType: WithType, withs: List<BuiltWith>) {
            sql
                .prefix(
                    when (withType) {
                        WithType.RECURSIVE -> "WITH RECURSIVE "
                        WithType.NOT_RECURSIVE -> "WITH "
                    },
                    "\n, "
                )
                .forEach(withs) {
                    sql.addSql(scope[it.cte])

                    when (val query = it.query) {
                        is BuiltValuesQuery -> compileRelabels(query.columns)
                        else -> { }
                    }

                    sql.addSql(" AS (")

                    val innerScope = scope.innerScope()

                    it.query.populateScope(innerScope)

                    Compilation(
                        scope = innerScope,
                        sql = sql
                    ).compileQuery(emptyList(), it.query, false)

                    sql.addSql(")")
                }
        }

        fun compileSelect(outerSelect: List<SelectedExpr<*>>, select: BuiltSelectQuery) {
            val withs = select.body.withs

            compileWiths(select.body.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            sql.selectClause(select.selected.takeIf { it.isNotEmpty() }?:outerSelect) {
                compileExpr(it.expr, false)
                sql.addSql(" ")
                sql.addSql(scope.nameOf(it.name))
            }

            if (select.body.relation.relation != EmptyRelation) sql.addSql("\nFROM ")

            sql.compileQueryBody(
                select.body,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) },
                compileSetOperation = { compileSetOperation(select.selected, it) }
            )
        }

        fun compileValues(query: BuiltValuesQuery, forInsert: Boolean): Boolean {
            return sql.compileValues(query,
                compileExpr = { compileExpr(it, false) }
            ) { columns, row ->
                if (!forInsert) sql.addSql("ROW ")

                sql.compileRow(columns, row) { compileExpr(it, false) }
            }
        }

        fun compileInsert(insert: BuiltInsert): Boolean {
            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.compileInsertLine(insert)

            sql.addSql("\n")

            val relvar = insert.unwrapTable()

            val nonEmpty = compileQuery(emptyList(), insert.query, true)

            sql.compileOnConflict(insert.onConflict) { assignments ->
                val innerScope = scope.innerScope()

                relvar.columns.forEach {
                    innerScope.internal(it, it.symbol, null)
                }

                val updateCtx = Compilation(innerScope, sql)

                sql.prefix(" ", "\n,").forEach(assignments) {
                    updateCtx.compileExpr(it.reference)
                    sql.addSql(" = ")
                    updateCtx.compileExpr(it.expr)
                }
            }

            return nonEmpty
        }

        fun compileUpdate(update: BuiltUpdate) {
            val query = update.query

            compileWiths(query.withType, query.withs)

            if (query.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("UPDATE ")

            compileRelation(update.query.relation)

            sql.addSql("\nSET ")

            val updatePrefix = sql.prefix("", ", ")

            check(query.joins.isEmpty()) {
                "dialect does not support JOIN in update"
            }

            update.assignments
                .forEach {
                    updatePrefix.next {
                        compileExpr(it.reference, false)
                        sql.addSql(" = ")
                        compileExpr(it.expr)
                    }
                }

            query.where?.let {
                sql.addSql("\nWHERE ")
                compileExpr(it, false)
            }
        }

        fun compileWindows(windows: List<LabeledWindow>) {
            sql.prefix("\nWINDOW ", "\n, ").forEach(windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(BuiltWindow.from(it.window))
                sql.addSql(")")
            }
        }

        fun compileDelete(delete: BuiltDelete) {
            val withs = delete.query.withs

            compileWiths(delete.query.withType, withs)

            if (withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("\nDELETE FROM ")

            sql.compileQueryBody(
                delete.query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) }
            )
        }
    }

    override fun compile(dml: BuiltDml): SqlText? {
        val registry = NameRegistry()
        val scope = Scope(registry)

        val compilation = Compilation(
            scope = scope
        )

        dml.populateScope(scope)

        val nonEmpty = when (dml) {
            is BuiltSelectQuery -> {
                compilation.compileSelect(emptyList(), dml)
                true
            }
            is BuiltValuesQuery -> compilation.compileValues(dml, false)
            is BuiltInsert -> compilation.compileInsert(dml)
            is BuiltUpdate -> {
                compilation.compileUpdate(dml)
                true
            }
            is BuiltDelete -> {
                compilation.compileDelete(dml)
                true
            }
        }

        return if (nonEmpty) {
            compilation.sql.toSql()
        } else {
            null
        }
    }
}