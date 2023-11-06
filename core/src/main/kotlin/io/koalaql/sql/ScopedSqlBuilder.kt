package io.koalaql.sql

import io.koalaql.Assignment
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.TableName
import io.koalaql.dsl.value
import io.koalaql.expr.*
import io.koalaql.identifier.Named
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.values.ValuesRow
import io.koalaql.window.*
import io.koalaql.window.built.BuiltWindow

class ScopedSqlBuilder(
    val output: CompiledSqlBuilder,
    val scope: Scope,
    val compiler: Compiler
) {
    fun addSql(sql: String) {
        output.addSql(sql)
    }

    fun addAlias(alias: Alias) {
        output.addIdentifier(scope[alias])
    }

    fun parenthesize(emitParens: Boolean = true, block: () -> Unit) {
        if (!emitParens) return block()

        output.addSql("(")
        block()
        output.addSql(")")
    }

    fun prefix(initial: String, after: String): SqlPrefix {
        var pref = initial

        return object : SqlPrefix {
            override fun next(block: () -> Unit) {
                addSql(pref)
                block()
                pref = after
            }
        }
    }

    fun addError(error: String) = output.addError(error)

    fun selectFields(
        fields: List<SelectedExpr<*>>,
        compileExpr: (Expr<*>) -> Unit
    ) {
        if (fields.isNotEmpty()) {
            prefix("", "\n, ").forEach(fields) {
                val resolved = when (it.expr) {
                    is AsReference -> scope.resolveOrNull(it.asReference())
                    else -> null
                }

                val relabel = scope.nameOf(it.name)

                compileExpr(it.expr)

                if (resolved?.innerName != relabel) {
                    addSql(" ")
                    output.addIdentifier(scope.nameOf(it.name))
                }
            }
        } else {
            addError("unable to generate empty selection")
        }
    }

    fun selectClause(
        query: BuiltSelectQuery,
        compileExpr: (Expr<*>) -> Unit
    ) {
        val selected = query.selected

        addSql("SELECT ")

        if (query.distinctness == Distinctness.DISTINCT) addSql("DISTINCT ")

        selectFields(selected, compileExpr)
    }

    fun returningClause(
        query: BuiltReturning,
        compileExpr: (Expr<*>) -> Unit
    ) {
        val returned = query.returned

        addSql("RETURNING ")

        selectFields(returned, compileExpr)
    }

    fun compileRangeMarker(direction: String, marker: FrameRangeMarker<*>, compileExpr: (Expr<*>) -> Unit) {
        when (marker) {
            CurrentRow -> addSql("CURRENT ROW")
            is Following<*> -> compileExpr(marker.offset)
            is Preceding<*> -> compileExpr(marker.offset)
            Unbounded -> addSql("UNBOUNDED $direction")
        }
    }

    fun compileWindow(
        window: BuiltWindow,
        compileExpr: (Expr<*>) -> Unit,
        compileOrderBy: (List<Ordinal<*>>) -> Unit
    ) {
        val partitionedBy = window.partitions.partitions
        val orderBy = window.partitions.orderBy

        val prefix = prefix("", " ")

        window.partitions.from?.let {
            prefix.next {
                addWindow(it)
            }
        }

        if (partitionedBy.isNotEmpty()) prefix.next {
            prefix("PARTITION BY ", ", ").forEach(partitionedBy) {
                compileExpr(it)
            }
        }

        if (orderBy.isNotEmpty()) prefix.next {
            compileOrderBy(orderBy)
        }

        window.type?.let { windowType ->
            prefix.next {
                addSql(windowType.sql)
                addSql(" ")

                val until = window.until

                if (until == null) {
                    compileRangeMarker("PRECEDING", window.from) { compileExpr(it) }
                } else {
                    addSql("BETWEEN ")
                    compileRangeMarker("PRECEDING", window.from) { compileExpr(it) }
                    addSql(" AND ")
                    compileRangeMarker("FOLLOWING", until) { compileExpr(it) }
                }
            }
        }
    }

    fun compileWindowClause(
        windows: List<LabeledWindow>,
        compileWindow: (BuiltWindow) -> Unit
    ) {
        prefix("\nWINDOW ", "\n, ").forEach(windows) { window ->
            addWindow(window.label)
            addSql(" AS ")
            addSql("(")
            compileWindow(BuiltWindow.from(window.window))
            addSql(")")
        }
    }

    fun compileJoins(
        joins: Iterable<BuiltJoin>,
        compileRelation: (BuiltRelation) -> Unit,
        compileExpr: (Expr<*>) -> Unit
    ) {
        joins.forEach { join ->
            addSql("\n")
            addSql(join.type.sql)
            addSql(" ")
            compileRelation(join.to)
            join.on?.let { on ->
                addSql(" ON ")
                compileExpr(on)
            }
        }
    }

    fun compileFullQuery(
        query: BuiltQuery,
        compileWiths: (BuiltWithable) -> Unit,
        compileSubquery: (BuiltUnionOperandQuery) -> Boolean,
        compileSetOperation: (BuiltSetOperation) -> Unit = {
            addSql("\n")
            addSql(it.type.sql)

            when (it.distinctness) {
                Distinctness.ALL -> addSql(" ALL")
                Distinctness.DISTINCT -> { }
            }

            addSql("\n")

            compileSubquery(it.body)
        },
        compileOrderBy: (List<Ordinal<*>>) -> Unit
    ): Boolean {
        compileWiths(query)

        val nonEmptyHead = compileSubquery(query.head)

        query.unioned.forEach(compileSetOperation)

        if (query.orderBy.isNotEmpty()) {
            addSql("\n")
            compileOrderBy(query.orderBy)
        }

        query.limit?.let {
            addSql("\nLIMIT ")
            compiler.addLiteral(this, value(it))
        }

        if (query.offset != 0) {
            addSql("\nOFFSET ")
            compiler.addLiteral(this, value(query.offset))
        }

        return nonEmptyHead || query.unioned.isNotEmpty()
    }

    fun compileUpdate(
        update: BuiltUpdate,
        compileWiths: (BuiltWithable) -> Unit,
        compileRelation: (BuiltRelation) -> Unit,
        compileJoins: (List<BuiltJoin>) -> Unit,
        compileAssignment: (Assignment<*>) -> Unit,
        compileExpr: (Expr<*>) -> Unit
    ): Boolean {
        val query = update.query

        compileWiths(update)

        if (update.withs.isNotEmpty()) addSql("\n")

        addSql("UPDATE ")

        compileRelation(update.query.relation)
        compileJoins(update.query.joins)

        addSql("\nSET ")

        val wasNonEmpty = if (update.assignments.isEmpty()) {
            addError("empty assignment list")

            false
        } else {
            prefix("", ", ").forEach(update.assignments) {
                compileAssignment(it)
            }

            true
        }

        query.where?.let {
            addSql("\nWHERE ")
            compileExpr(it)
        }

        return wasNonEmpty
    }

    fun compileOrderBy(
        ordinals: List<Ordinal<*>>,
        compileExpr: (Expr<*>) -> Unit
    ) {
        prefix("ORDER BY ", ", ").forEach(ordinals) {
            val orderKey = it.toOrderKey()

            compileExpr(orderKey.expr)

            addSql(" ${orderKey.order.sql}")

            when (orderKey.nulls) {
                NullOrdering.FIRST -> addSql(" NULLS FIRST")
                NullOrdering.LAST -> addSql(" NULLS LAST")
                null -> { }
            }
        }
    }

    fun compileInsertLine(
        insert: BuiltInsert,
        table: TableRelation = insert.unwrapTable(),
        compileName: () -> Unit = { addTableReference(table) }
    ) {
        val columns = insert.query.columns

        if (insert.ignore) {
            addSql("INSERT IGNORE INTO ")
        } else {
            addSql("INSERT INTO ")
        }

        val tableColumnMap = table.columns.associateBy { it }

        compileName()

        parenthesize {
            prefix("", ", ").forEach(columns) {
                val column = checkNotNull(tableColumnMap[it]) {
                    "can't insert $it into ${table.tableName}"
                }

                addIdentifier(column.symbol)
            }
        }
    }

    fun compileQueryBody(
        body: BuiltQueryBody,
        compileExpr: (Expr<*>) -> Unit,
        compileRelation: (BuiltRelation) -> Unit,
        compileWindows: (windows: List<LabeledWindow>) -> Unit,
        compileJoins: (List<BuiltJoin>) -> Unit = { joins ->
            this.compileJoins(joins, compileRelation, compileExpr)
        },
        compileWhere: (Expr<*>) -> Unit = { where ->
            addSql("\nWHERE ")
            compileExpr(where)
        },
        compileGroupBy: (List<Expr<*>>) -> Unit = {
            prefix("\nGROUP BY ", ", ").forEach(body.groupBy) {
                compileExpr(it)
            }
        },
        compileHaving: (Expr<*>) -> Unit = {
            addSql("\nHAVING ")
            compileExpr(it)
        },
        compileOrderBy: (List<Ordinal<*>>) -> Unit = { orderBys ->
            this.compileOrderBy(orderBys) {
                compileExpr(it)
            }
        }
    ) {
        compileRelation(body.relation)

        if (body.joins.isNotEmpty()) compileJoins(body.joins)
        body.where?.let(compileWhere)

        if (body.groupBy.isNotEmpty()) compileGroupBy(body.groupBy)
        body.having?.let(compileHaving)

        if (body.windows.isNotEmpty()) compileWindows(body.windows)

        if (body.orderBy.isNotEmpty()) {
            addSql("\n")
            compileOrderBy(body.orderBy)
        }

        body.limit?.let {
            addSql("\nLIMIT ")
            compiler.addLiteral(this, value(it))
        }

        if (body.offset != 0) {
            addSql("\nOFFSET ")
            compiler.addLiteral(this, value(body.offset))
        }

        body.locking?.let { locking ->
            when (locking) {
                LockMode.SHARE -> addSql("\nFOR SHARE")
                LockMode.UPDATE -> addSql("\nFOR UPDATE")
            }
        }
    }

    fun compileRow(
        columns: List<Reference<*>>,
        row: ValuesRow,
        compileExpr: (Expr<*>) -> Unit
    ) {
        addSql("(")
        prefix("", ", ").forEach(columns) {
            @Suppress("unchecked_cast")
            compileExpr(row[it])
        }
        addSql(")")
    }

    fun compileValues(
        query: BuiltValuesQuery,
        compileExpr: (Expr<*>) -> Unit,
        compileRow: (List<Reference<*>>, ValuesRow) -> Unit = { columns, it ->
            this.compileRow(columns, it, compileExpr)
        }
    ): Boolean {
        val values = query.values

        addSql("VALUES ")

        val iter = values.valuesIterator()
        var count = 0

        return if (iter.next()) {
            val rowPrefix = prefix("", "\n, ")

            do {
                if (count == 1) output.beginAbridgement()

                rowPrefix.next {
                    compileRow(values.columns, iter.row)
                }

                count++
            } while (iter.next())

            if (count > 1) output.endAbridgement(" /* VALUES had ${count - 1} more rows here */")

            true
        } else {
            addError("couldn't generate empty values")

            false
        }
    }

    fun addColumnMappings(columns: List<TableColumn<*>>) {
        columns.forEach { output.addMapping(it.builtDef.columnType) }
    }

    fun addTableName(name: TableName) {
        name.schema?.let {
            addIdentifier(it)
            addSql(".")
        }
        addIdentifier(name.name)
    }

    fun addIdentifier(id: String) = output.addIdentifier(Named(id))

    fun addTableReference(table: TableRelation) {
        addColumnMappings(table.columns)
        addTableName(table.tableName)
    }

    private fun compileOnConflictKey(
        key: OnConflictKey,
        compileExpr: (Expr<*>) -> Unit
    ) {
        when (key) {
            is OnConflictKeyIndex -> {
                addSql("\nON CONFLICT ON CONSTRAINT ")
                addIdentifier(key.index.name)
            }
            is OnConflictKeyColumns -> {
                addSql("\nON CONFLICT ")
                parenthesize {
                    prefix("", ", ").forEach(key.columns) {
                        compileExpr(it)
                    }
                }

                key.where?.let {
                    addSql("\nWHERE ")
                    compileExpr(it)
                }
            }
        }
    }

    fun compileOnConflict(
        onConflict: OnConflictOrDuplicateAction?,
        compileAssignment: (Assignment<*>) -> Unit,
        compileExpr: (Expr<*>) -> Unit
    ) {
        val assignments = when (onConflict) {
            is OnConflictIgnore -> {
                compileOnConflictKey(onConflict.key, compileExpr)
                addSql(" DO NOTHING")
                return
            }
            is OnConflictUpdate -> {
                compileOnConflictKey(onConflict.key, compileExpr)
                addSql(" DO UPDATE SET")

                onConflict.assignments
            }
            is OnDuplicateUpdate -> {
                addSql("\nON DUPLICATE KEY UPDATE")

                onConflict.assignments
            }
            null -> return
        }

        if (assignments.isNotEmpty()) {
            prefix(" ", "\n,").forEach(assignments) {
                compileAssignment(it)
            }
        } else {
            addError("empty assignment list")
        }
    }

    fun compileInsert(
        insert: BuiltInsert,
        compileInsertLine: (BuiltInsert) -> Unit,
        compileQuery: (BuiltQuery) -> Boolean,
        compileOnConflict: (OnConflictOrDuplicateAction) -> Unit
    ): Boolean {
        compileInsertLine(insert)

        addSql("\n")

        val nonEmpty = compileQuery(insert.query)

        insert.onConflict?.let(compileOnConflict)

        return nonEmpty
    }

    fun compileDelete(
        delete: BuiltDelete,
        compileWiths: (BuiltWithable) -> Unit,
        compileQueryBody: (BuiltQueryBody) -> Unit
    ) {
        compileWiths(delete)

        addSql("DELETE FROM ")

        compileQueryBody(delete.query)
    }

    fun compileWiths(
        withable: BuiltWithable,
        compileCte: (Cte) -> Unit,
        compileRelabels : (List<Reference<*>>) -> Unit,
        compileQuery: (BuiltSubquery) -> Boolean
    ) {
        val prefix = prefix(
            when (withable.withType) {
                WithType.RECURSIVE -> "WITH RECURSIVE "
                WithType.NOT_RECURSIVE -> "WITH "
            },
            "\n, "
        )

        prefix.forEach(withable.withs) {
            compileCte(it.cte)

            if (it.query.columnsUnnamed()) compileRelabels(it.query.columns)

            addSql(" AS (")

            compileQuery(it.query)

            addSql(")")
        }
    }

    fun transformScope(operation: (Scope) -> Scope) =
        ScopedSqlBuilder(output, operation(scope), compiler)

    fun withScope(query: PopulatesScope) = transformScope { scope ->
        val innerScope = scope.innerScope()

        query.populateScope(innerScope)

        innerScope
    }

    fun withCtes(withable: BuiltWithable) = transformScope { scope ->
        val innerScope = scope.innerScope()

        withable.withs.forEach {
            innerScope.cte(it.cte, it.query.columns)
        }

        innerScope
    }

    fun withColumns(columns: List<Column<*>>, alias: Alias? = null) = transformScope { scope ->
        val innerScope = scope.innerScope()

        columns.forEach {
            innerScope.internal(it, Named(it.symbol), alias)
        }

        innerScope
    }

    fun addReference(reference: Reference<*>) {
        output.addIdentifier(scope.nameOf(reference))
    }

    fun resolveReference(reference: Reference<*>) {
        output.withResult(scope.resolve(reference)) {
            output.addResolved(it)
        }
    }

    fun resolveWithoutAlias(reference: Reference<*>) {
        output.withResult(scope.resolve(reference)) {
            output.addIdentifier(it.innerName)
        }
    }

    fun addCte(cte: Cte) {
        output.addIdentifier(scope[cte])
    }

    fun addWindow(window: WindowLabel) {
        output.addIdentifier(scope.nameOf(window))
    }

    private inline fun <T> scopedIn(query: PopulatesScope, block: ScopedSqlBuilder.() -> T): T {
        return withScope(query).block()
    }

    fun compileReturning(
        dml: BuiltReturning,
        compileStmt: ScopedSqlBuilder.(BuiltStatement) -> Boolean,
        compileExpr: ScopedSqlBuilder.(Expr<*>) -> Unit
    ): Boolean = scopedIn(dml.stmt) {
        val nonEmpty = compileStmt(dml.stmt)

        addSql("\n")

        returningClause(dml) { compileExpr(it) }

        nonEmpty
    }

    fun compile(
        dml: BuiltDml,
        compileQuery: ScopedSqlBuilder.(BuiltSubquery) -> Boolean,
        compileStmt: ScopedSqlBuilder.(BuiltStatement) -> Boolean
    ): CompiledSql? {
        val nonEmpty = when (dml) {
            is BuiltSubquery -> compileQuery(dml)
            is BuiltStatement -> scopedIn(dml) { compileStmt(dml) }
        }

        return if (nonEmpty) {
            toSql()
        } else {
            null
        }
    }

    fun toSql() = output.toSql()
}