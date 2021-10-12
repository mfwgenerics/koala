package io.koalaql.dialect

import io.koalaql.Assignment
import io.koalaql.expr.*
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.RawSqlBuilder
import io.koalaql.sql.SqlTextBuilder
import io.koalaql.values.RowIterator
import io.koalaql.window.*
import kotlin.reflect.KClass

fun SqlTextBuilder.selectClause(selected: List<SelectedExpr<*>>, compileSelect: (SelectedExpr<*>) -> Unit) {
    val selectPrefix = prefix("SELECT ", "\n, ")

    (selected)
        .forEach {
            selectPrefix.next {
                compileSelect(it)
            }
        }
}

fun SqlTextBuilder.compileRangeMarker(direction: String, marker: FrameRangeMarker<*>, compileExpr: (Expr<*>) -> Unit) {
    when (marker) {
        CurrentRow -> addSql("CURRENT ROW")
        is Following<*> -> compileExpr(marker.offset)
        is Preceding<*> -> compileExpr(marker.offset)
        Unbounded -> addSql("UNBOUNDED $direction")
    }
}

fun SqlTextBuilder.compileJoins(
    joins: Iterable<BuiltJoin>,
    compileRelation: (BuiltRelation) -> Unit,
    compileExpr: (Expr<*>) -> Unit
) {
    joins.forEach { join ->
        addSql("\n")
        addSql(join.type.sql)
        addSql(" ")
        compileRelation(join.to)
        addSql(" ON ")
        compileExpr(join.on)
    }
}

fun SqlTextBuilder.compileExpr(
    expr: QuasiExpr,
    emitParens: Boolean,
    impl: ExpressionCompiler
) {
    when (expr) {
        is AggregatedExpr<*> -> {
            val aggregated = expr.buildAggregated()

            addSql(aggregated.expr.type.sql)
            parenthesize {
                prefix("", ", ").forEach(aggregated.expr.args) {
                    impl.aggregatable(false, it)
                }
            }

            aggregated.filter?.let { filter ->
                addSql(" FILTER(WHERE ")

                compileExpr(filter, false, impl)

                addSql(")")
            }

            aggregated.over?.let { window ->
                addSql(" OVER (")
                impl.window(window)
                addSql(")")
            }
        }
        is CastExpr<*> -> {
            addSql("CAST")
            parenthesize {
                compileExpr(expr.of, false, impl)
                addSql(" AS ")
                impl.dataTypeForCast(expr.type)
            }
        }
        is ComparedQuery<*> -> {
            addSql(expr.type)
            impl.subquery(false, expr.subquery)
        }
        is ExprListExpr<*> -> {
            parenthesize {
                prefix("", ", ").forEach(expr.exprs) {
                    compileExpr(it, false, impl)
                }
            }
        }
        is Literal<*> -> addLiteral(expr)
        is OperationExpr<*> -> {
            when (expr.type.fixity) {
                OperationFixity.PREFIX -> parenthesize(emitParens) {
                    addSql(expr.type.sql)
                    addSql(" ")

                    compileExpr(expr.args.single(), false, impl)
                }
                OperationFixity.POSTFIX -> parenthesize(emitParens) {
                    compileExpr(expr.args.single(), false, impl)
                    addSql(" ")
                    addSql(expr.type.sql)
                }
                OperationFixity.INFIX -> parenthesize(emitParens) {
                    prefix("", " ${expr.type.sql} ").forEach(expr.args) {
                        compileExpr(it, true, impl)
                    }
                }
                OperationFixity.APPLY -> {
                    addSql(expr.type.sql)
                    parenthesize {
                        prefix("", ", ").forEach(expr.args) {
                            compileExpr(it, false, impl)
                        }
                    }
                }
            }
        }
        is AsReference<*> -> {
            val reference = expr.asReference()
            val excluded = reference.excludedReference()

            if (excluded != null) {
                impl.excluded(excluded)
            } else {
                impl.reference(false, reference)
            }
        }
        is SubqueryExpr<*> -> {
            impl.subquery(false, expr.buildQuery())
        }
        is CaseExpr<*, *> -> parenthesize(emitParens) {
            addSql("CASE ")

            if (!expr.isGeneralCase) compileExpr(expr.onExpr, true, impl)

            expr.cases.forEach { whenThen ->
                addSql("\nWHEN ")
                compileExpr(whenThen.whenExpr, false, impl)
                addSql(" THEN ")
                compileExpr(whenThen.thenExpr, true, impl)
            }

            expr.elseExpr?.let {
                addSql("\nELSE ")
                compileExpr(it, false, impl)
            }

            addSql("\nEND")
        }
        is RawExpr<*> -> {
            val build = expr.build

            object : RawSqlBuilder {
                override fun sql(value: String) { addSql(value) }
                override fun expr(expr: QuasiExpr) { compileExpr(expr, true, impl) }
            }.build()
        }
        else -> error("missed case $expr")
    }
}

fun SqlTextBuilder.compileOrderBy(ordinals: List<Ordinal<*>>, compileExpr: (Expr<*>) -> Unit) {
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

fun SqlTextBuilder.compileInsertLine(
    insert: BuiltInsert,
    table: Relvar = insert.unwrapTable(),
    compileName: () -> Unit = { addIdentifier(table.relvarName) }
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
                "can't insert $it into ${table.relvarName}"
            }

            addIdentifier(column.symbol)
        }
    }
}

fun SqlTextBuilder.compileQueryBody(
    body: BuiltQueryBody,
    compileExpr: (Expr<*>) -> Unit,
    compileRelation: (BuiltRelation) -> Unit,
    compileWindows: (windows: List<LabeledWindow>) -> Unit,
    compileJoins: (List<BuiltJoin>) -> Unit = { joins ->
        joins.asReversed().forEach { join ->
            addSql("\n")
            addSql(join.type.sql)
            addSql(" ")
            compileRelation(join.to)
            addSql(" ON ")
            compileExpr(join.on)
        }
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
    }
) {
    compileRelation(body.relation)

    if (body.joins.isNotEmpty()) compileJoins(body.joins)
    body.where?.let(compileWhere)

    if (body.groupBy.isNotEmpty()) compileGroupBy(body.groupBy)
    body.having?.let(compileHaving)

    if (body.windows.isNotEmpty()) compileWindows(body.windows)
}

fun SqlTextBuilder.compileRow(
    iter: RowIterator
) {
    addSql("(")
    prefix("", ", ").forEach(iter.columns) {
        @Suppress("unchecked_cast")
        addLiteral(Literal(
            it.type as KClass<Any>,
            iter.row.getOrNull(it)
        ))
    }
    addSql(")")
}

fun SqlTextBuilder.compileValues(
    query: BuiltValuesQuery,
    emptyValues: (LabelList) -> Unit = {
        check(it.isNotEmpty()) { "empty VALUES with no columns" }

        prefix("SELECT ", ",").forEach(it) {
            addSql("NULL")
        }

        addSql(" LIMIT 0")
    },
    compileRow: (RowIterator) -> Unit = { this.compileRow(it) }
) {
    val values = query.values

    val iter = values.rowIterator()

    if (iter.next()) {
        val rowPrefix = prefix("VALUES ", "\n, ")

        do {
            rowPrefix.next {
                compileRow(iter)
            }
        } while (iter.next())
    } else {
        emptyValues(values.columns)
    }
}

fun SqlTextBuilder.compileOnConflict(
    onConflict: OnConflictOrDuplicateAction?,
    compileAssignments: (List<Assignment<*>>) -> Unit
) {
    when (onConflict) {
        is OnConflictIgnore -> {
            addSql("\nON CONFLICT ON CONSTRAINT ")
            addIdentifier(onConflict.key.name)
            addSql(" DO NOTHING")
        }
        is OnConflictUpdate -> {
            addSql("\nON CONFLICT ON CONSTRAINT ")
            addIdentifier(onConflict.key.name)

            addSql(" DO UPDATE SET")

            compileAssignments(onConflict.assignments)
        }
        is OnDuplicateUpdate -> {
            addSql("\nON DUPLICATE KEY UPDATE")

            compileAssignments(onConflict.assignments)
        }
        null -> { }
    }
}