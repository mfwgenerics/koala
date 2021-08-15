package mfwgenerics.kotq

sealed interface Ordinal<T : Any> {
    fun toOrderKey(): OrderKey<T>
}

enum class SortOrder {
    ASC,
    DESC
}

class OrderKey<T : Any>(
    val order: SortOrder,
    val expr: Expr<T>
) : Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = this
}

sealed interface Expr<T : Any>: Ordinal<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)
}

sealed interface Reference<T : Any>: Expr<T>, ReferenceGroup

class AliasedReference<T : Any>(
    val of: Alias,
    val reference: Reference<T>
): Reference<T>

abstract class Column<T : Any>: Reference<T>
abstract class Projection<T : Any>: Reference<T>
class Selected<T : Any>(val expr: Expr<T>): Reference<T>