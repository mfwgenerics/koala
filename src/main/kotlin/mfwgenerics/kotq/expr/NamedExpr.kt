package mfwgenerics.kotq.expr

sealed interface Selection

object SelectAll: Selection

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Reference<T>
): NamedExprs, Selection {
    override fun namedExprs(): List<SelectedExpr<*>> = listOf(this)
}

infix fun <T : Any> Expr<T>.`as`(reference: Reference<T>): SelectedExpr<T> =
    SelectedExpr(this, reference)