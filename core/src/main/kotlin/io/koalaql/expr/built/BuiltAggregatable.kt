package io.koalaql.expr.built

import io.koalaql.expr.Expr
import io.koalaql.expr.Ordinal
import io.koalaql.query.Distinctness

class BuiltAggregatable {
    lateinit var expr: Expr<*>
    var distinct: Distinctness = Distinctness.ALL
    var orderBy: List<Ordinal<*>> = emptyList()
}