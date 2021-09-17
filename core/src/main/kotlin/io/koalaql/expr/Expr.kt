package io.koalaql.expr

import io.koalaql.expr.built.BuiltAggregatable

sealed interface Expr<T : Any>: ComparisonOperand<T>, Ordinal<T>, OrderableAggregatable<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)

    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.expr = this
        return null
    }
}