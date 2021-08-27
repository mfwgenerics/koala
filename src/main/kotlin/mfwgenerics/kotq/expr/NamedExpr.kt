package mfwgenerics.kotq.expr

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Reference<T>
): NamedExprs {
    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.expression(expr, name)
    }
}

infix fun <T : Any> Expr<T>.`as`(reference: Reference<T>): SelectedExpr<T> =
    SelectedExpr(this, reference)