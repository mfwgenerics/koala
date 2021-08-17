package mfwgenerics.kotq.expr

import mfwgenerics.kotq.Distinctness

class BuiltAggregatable {
    lateinit var expr: Expr<*>
    var distinct: Distinctness = Distinctness.ALL
    var orderBy: List<Ordinal<*>> = emptyList()
}