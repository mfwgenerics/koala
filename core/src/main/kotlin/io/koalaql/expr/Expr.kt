package io.koalaql.expr

import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.expr.fluent.NullsOrderable

sealed interface Expr<T : Any>: ComparisonOperand<T>, NullsOrderable<T>, OrderableAggregatable<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(this, SortOrder.ASC)

    fun asc(): NullsOrderable<T> = NullsOrderableOrderKey(OrderKey(this, SortOrder.ASC))
    fun desc(): NullsOrderable<T> = NullsOrderableOrderKey(OrderKey(this, SortOrder.DESC))

    override fun BuiltAggregatable.buildIntoAggregatable(): AggregatableBuilder? {
        expr = this@Expr
        return null
    }
}