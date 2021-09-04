package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.built.BuiltJoin
import mfwgenerics.kotq.query.built.BuiltRelation
import mfwgenerics.kotq.sql.SqlTextBuilder
import mfwgenerics.kotq.window.*

fun SqlTextBuilder.selectClause(selected: List<SelectedExpr<*>>, compileSelect: (SelectedExpr<*>) -> Unit) {
    val selectPrefix = prefix("SELECT ", "\n, ")

    (selected)
        .forEach {
            selectPrefix.next {
                compileSelect(it)
            }
        }
}

fun SqlTextBuilder.orderByClause(ordinals: List<Ordinal<*>>, compileExpr: (Expr<*>) -> Unit) {
    prefix("ORDER BY ", ", ").forEach(ordinals) {
        val orderKey = it.toOrderKey()

        compileExpr(orderKey.expr)

        addSql(" ${orderKey.order.sql}")
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