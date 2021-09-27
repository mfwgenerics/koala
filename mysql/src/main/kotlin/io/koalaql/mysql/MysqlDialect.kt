package io.koalaql.mysql

import io.koalaql.data.*
import io.koalaql.ddl.IndexType
import io.koalaql.ddl.Table
import io.koalaql.ddl.TableColumn
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
                    sql.addIdentifier(table.newTable.relvarName)
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
                    sql.addIdentifier(table.newTable.relvarName)
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
                    sql.addIdentifier(table.newTable.relvarName)
                    sql.addSql(" DROP COLUMN ")
                    sql.addIdentifier(column)
                }
            }

            table.indexes.created.forEach { (name, index) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addIdentifier(table.newTable.relvarName)
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
                    sql.addIdentifier(table.newTable.relvarName)
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
                is RelvarColumn<*> -> sql.addIdentifier(expr.symbol)
                else -> compileExpr(expr)
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

            sql.addSql(table.relvarName)
            sql.parenthesize {
                val comma = sql.prefix("\n", ",\n")

                comma.forEach(table.columns) {
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
            sql.addResolved(scope.resolve(name))
        }

        fun compileOrderBy(ordinals: List<Ordinal<*>>) = sql.orderByClause(ordinals) {
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

        fun compileQuery(outerSelect: List<SelectedExpr<*>>, query: BuiltSubquery, forInsert: Boolean) {
            val innerScope = scope.innerScope()

            query.populateScope(innerScope)

            val compilation = Compilation(
                sql = sql,
                scope = innerScope
            )

            when (query) {
                is BuiltSelectQuery -> compilation.compileSelect(outerSelect, query)
                is BuiltValuesQuery -> compilation.compileValues(query, forInsert)
            }
        }

        fun compileSubqueryExpr(subquery: BuiltSubquery) {
            sql.parenthesize {
                compileQuery(emptyList(), subquery, false)
            }
        }

        fun compileExpr(expr: QuasiExpr, emitParens: Boolean = true) {
            sql.compileExpr(expr, emitParens, this)
        }

        override fun excluded(reference: Reference<*>) {
            sql.addSql("VALUES")
            sql.parenthesize {
                compileReference(reference)
            }
        }

        fun compileRelation(relation: BuiltRelation) {
            val explicitLabels = when (val baseRelation = relation.relation) {
                is Relvar -> {
                    sql.addSql(baseRelation.relvarName)
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

        fun compileQueryWhere(query: BuiltQueryBody) {
            compileRelation(query.relation)

            sql.compileJoins(query.joins.asReversed(),
                compileRelation = { compileRelation(it) },
                compileExpr = { compileExpr(it, false) }
            )

            query.where?.let {
                sql.addSql("\nWHERE ")
                compileExpr(it, false)
            }
        }

        fun compileSelectBody(body: BuiltQueryBody) {
            compileQueryWhere(body)

            sql.prefix("\nGROUP BY ", ", ").forEach(body.groupBy) {
                compileExpr(it, false)
            }

            body.having?.let {
                sql.addSql("\nHAVING ")
                compileExpr(it, false)
            }

            sql.prefix("\nWINDOW ", "\n, ").forEach(body.windows) {
                sql.addSql(scope.nameOf(it.label))
                sql.addSql(" AS ")
                sql.addSql("(")
                compileWindow(it.window.buildWindow())
                sql.addSql(")")
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

        fun compileRelabels(labels: LabelList) {
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

            compileSelectBody(select.body)

            select.body.setOperations.forEach {
                compileSetOperation(select.selected, it)
            }

            if (select.body.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(select.body.orderBy)

            select.body.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(value(it))
            }

            if (select.body.offset != 0) {
                check (select.body.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(select.body.offset))
            }

            select.body.locking?.let { locking ->
                when (locking) {
                    LockMode.SHARE -> sql.addSql("\nFOR UPDATE")
                    LockMode.UPDATE -> sql.addSql("\nFOR SHARED")
                }
            }
        }

        fun compileValues(query: BuiltValuesQuery, forInsert: Boolean) {
            val values = query.values

            val columns = values.columns
            val iter = values.rowIterator()

            val rowPrefix = sql.prefix("VALUES ", "\n, ")

            while (iter.next()) {
                rowPrefix.next {
                    if (!forInsert) sql.addSql("ROW ")

                    sql.addSql("(")
                    sql.prefix("", ", ").forEach(columns) {
                        @Suppress("unchecked_cast")
                        sql.addLiteral(Literal(
                            it.type as KClass<Any>,
                            iter.row.getOrNull(it)
                        ))
                    }
                    sql.addSql(")")
                }
            }
        }

        fun compileInsert(insert: BuiltInsert) {
            compileWiths(insert.withType, insert.withs)

            if (insert.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("INSERT INTO ")

            val relvar = when (val relation = insert.relation.relation) {
                is Relvar -> relation
                else -> error("insert not supported")
            }

            val tableColumnMap = relvar.columns.associateBy { it }
            val columns = insert.query.columns

            sql.addSql(relvar.relvarName)
            sql.addSql(" ")

            sql.parenthesize {
                sql.prefix("", ", ").forEach(columns) {
                    val column = checkNotNull(tableColumnMap[it]) {
                        "can't insert $it into ${relvar.relvarName}"
                    }

                    sql.addSql(column.symbol)
                }
            }

            sql.addSql("\n")

            compileQuery(emptyList(), insert.query, true)

            insert.onConflict?.let { onConflict ->
                check (onConflict is OnConflictAction.Update)
                    { "MySQL does not support onConflict Ignore" }

                check (onConflict.keys.isEmpty())
                    { "MySQL does not support onConflict keys" }

                sql.addSql("\nON DUPLICATE KEY UPDATE")

                val innerScope = scope.innerScope()

                relvar.columns.forEach {
                    innerScope.internal(it, it.symbol, null)
                }

                val updateCtx = Compilation(innerScope, sql)

                sql.prefix(" ", "\n,").forEach(onConflict.assignments) {
                    updateCtx.compileExpr(it.reference)
                    sql.addSql(" = ")
                    updateCtx.compileExpr(it.expr)
                }
            }
        }

        fun compileUpdate(update: BuiltUpdate) {
            val query = update.query

            compileWiths(query.withType, query.withs)

            if (query.withs.isNotEmpty()) sql.addSql("\n")

            sql.addSql("UPDATE ")

            compileRelation(update.query.relation)

            sql.addSql("\nSET ")

            val updatePrefix = sql.prefix("", ", ")

            check (query.joins.isEmpty()) {
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
                compileWindow(it.window.buildWindow())
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

            check (delete.query.setOperations.isEmpty())

            if (delete.query.orderBy.isNotEmpty()) sql.addSql("\n")
            compileOrderBy(delete.query.orderBy)

            delete.query.limit?.let {
                sql.addSql("\nLIMIT ")
                sql.addLiteral(value(it))
            }

            if (delete.query.offset != 0) {
                check (delete.query.limit != null) { "MySQL does not support OFFSET without LIMIT" }

                sql.addSql(" OFFSET ")
                sql.addLiteral(value(delete.query.offset))
            }

            check(delete.query.locking == null)
        }
    }

    override fun compile(statement: BuiltStatement): SqlText {
        val registry = NameRegistry()
        val scope = Scope(registry)

        val compilation = Compilation(
            scope = scope
        )

        statement.populateScope(scope)

        when (statement) {
            is BuiltSelectQuery -> compilation.compileSelect(emptyList(), statement)
            is BuiltValuesQuery -> compilation.compileValues(statement, false)
            is BuiltInsert -> compilation.compileInsert(statement)
            is BuiltUpdate -> compilation.compileUpdate(statement)
            is BuiltDelete -> compilation.compileDelete(statement)
        }

        return compilation.sql.toSql()
    }
}