package io.koalaql.expr.built

import io.koalaql.expr.AggregatableBuilder
import io.koalaql.expr.Expr
import io.koalaql.expr.Ordinal
import io.koalaql.query.Distinctness
import io.koalaql.unfoldBuilder

class BuiltAggregatable {
    lateinit var expr: Expr<*>
    var distinct: Distinctness = Distinctness.ALL
    var orderBy: List<Ordinal<*>> = emptyList()

    companion object {
        fun from(builder: AggregatableBuilder): BuiltAggregatable =
            unfoldBuilder(builder, BuiltAggregatable()) { it.buildIntoAggregatable() }
    }
}