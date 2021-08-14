package mfwgenerics.kotq

sealed interface Ordinal<T : Any> {
    val order: SortOrder
    val expr: Expr<T>
}

enum class SortOrder {
    ASC,
    DESC
}

class OrderKey<T : Any>(
    override val order: SortOrder,
    override val expr: Expr<T>
) : Ordinal<T>



sealed class Expr<T : Any>: Ordinal<T> {
    override val order: SortOrder get() = SortOrder.ASC
    override val expr: Expr<T> get() = this

    class Aliased<T : Any>(
        val alias: Alias,
        val reference: Reference<T>
    ): Expr<T>()
}

sealed class Reference<T : Any>: Expr<T>(), ReferenceGroup

abstract class Column<T : Any>: Reference<T>()
abstract class Select<T : Any>: Reference<T>()
