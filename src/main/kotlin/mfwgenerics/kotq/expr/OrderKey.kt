package mfwgenerics.kotq.expr

class OrderKey<T : Any>(
    val order: SortOrder,
    val expr: Expr<T>
) : Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = this
}