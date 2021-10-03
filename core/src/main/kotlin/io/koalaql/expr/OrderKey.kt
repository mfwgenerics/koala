package io.koalaql.expr

data class OrderKey<T : Any>(
    val expr: Expr<T>,
    val order: SortOrder,
    val nulls: NullOrdering? = null
): Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = this
}