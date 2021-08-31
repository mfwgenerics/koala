package mfwgenerics.kotq.expr.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.Distinctness

class BuiltAggregatable {
    lateinit var expr: Expr<*>
    var distinct: Distinctness = Distinctness.ALL
    var orderBy: List<Ordinal<*>> = emptyList()
}