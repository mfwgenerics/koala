package io.koalaql.mysql

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

        override fun subquery(emitParens: Boolean, subquery: BuiltQuery) {
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

        inline fun <T> scopedIn(query: PopulatesScope, block: Compilation.() -> T): T {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return compilation.block()
        }

        // TODO remove this after WITH changes
        private inline fun <T> scopedCtesIn(query: BuiltQuery, block: Compilation.() -> T): T {
            val innerScope = scope.innerScope()

            query.populateCtes(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            return compilation.block()
        }

        fun compileQuery(query: BuiltUnionOperandQuery, omitRow: Boolean = false): Boolean {
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

        fun compileQuery(query: BuiltQuery, forInsert: Boolean = false): Boolean {
            return scopedCtesIn(query) {
                sql.compileFullQuery(
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

        fun compileSubqueryExpr(subquery: BuiltQuery) {
            sql.parenthesize {
                compileQuery(subquery)
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
                    sql.addIdentifier(baseRelation.tableName)
                    null
                }
                is Subquery -> {
                    val innerScope = scope.innerScope()

                    baseRelation.of.populateScope(innerScope)

                    sql.parenthesize {
                        compileQuery(baseRelation.of)
                    }

                    if (baseRelation.of.columnsUnnamed()) {
                        baseRelation.of.columns
                    } else {
                        null
                    }
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

        fun compileRelabels(labels: List<Reference<*>>) {
            sql.parenthesize {
                sql.prefix("", ", ").forEach(labels) {
                    sql.addIdentifier(scope.nameOf(it))
                }
            }
        }

        fun compileWiths(withable: BuiltWithable) = sql.compileWiths(
            withable,
            compileCte = { sql.addSql(scope[it]) },
            compileRelabels = { compileRelabels(it) },
            compileQuery = { compileQuery(it) }
        )

        fun compileSelect(select: BuiltSelectQuery) {
            sql.selectClause(select.selected, scope) { compileExpr(it, false) }

            if (select.body.relation.relation != EmptyRelation) sql.addSql("\nFROM ")

            sql.compileQueryBody(
                select.body,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { windows -> compileWindows(windows) }
            )
        }

        fun compileValues(query: BuiltValuesQuery, omitRow: Boolean): Boolean {
            return sql.compileValues(query,
                compileExpr = { compileExpr(it, false) }
            ) { columns, row ->
                if (!omitRow) sql.addSql("ROW ")

                sql.compileRow(columns, row) { compileExpr(it, false) }
            }
        }

        fun compileInsert(insert: BuiltInsert): Boolean = sql.compileInsert(
            insert,
            compileInsertLine = { sql.compileInsertLine(insert) },
            compileQuery = { compileQuery(it, true) },
            compileOnConflict = {
                val relvar = insert.unwrapTable()

                sql.compileOnConflict(it) { assignments ->
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
            }
        )

        fun compileUpdate(update: BuiltUpdate) = sql.compileUpdate(update,
            compileWiths = { compileWiths(it) },
            compileRelation = { compileRelation(it) },
            compileAssignment = {
                compileExpr(it.reference, false)
                sql.addSql(" = ")
                compileExpr(it.expr)
            },
            compileExpr = { compileExpr(it, false) }
        )

        fun compileWindows(windows: List<LabeledWindow>) {
            sql.prefix("\nWINDOW ", "\n, ").forEach(windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(BuiltWindow.from(it.window))
                sql.addSql(")")
            }
        }

        fun compileDelete(delete: BuiltDelete) = sql.compileDelete(delete,
            compileWiths = { compileWiths(it) },
            compileQueryBody = { query ->
                sql.compileQueryBody(
                    query,
                    compileExpr = { compileExpr(it, false) },
                    compileRelation = { compileRelation(it) },
                    compileWindows = { compileWindows(it) }
                )
            }
        )
    }

    override fun compile(dml: BuiltDml): SqlText? {
        return with(Compilation(scope = Scope(NameRegistry { "column_$it" }))) {
            val nonEmpty = when (dml) {
                is BuiltQuery -> compileQuery(dml)
                is BuiltInsert -> scopedIn(dml) { compileInsert(dml) }
                is BuiltUpdate -> scopedIn(dml) { compileUpdate(dml) }
                is BuiltDelete -> {
                    scopedIn(dml) { compileDelete(dml) }
                    true
                }
            }

            if (nonEmpty) {
                sql.toSql()
            } else {
                null
            }
        }
    }
}