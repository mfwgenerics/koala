package io.koalaql.expr

import io.koalaql.expr.fluent.NullsOrderable

class NullsOrderableOrderKey<T : Any>(
    private val orderKey: OrderKey<T>
): NullsOrderable<T> {
    override fun toOrderKey(): OrderKey<T> = orderKey
}