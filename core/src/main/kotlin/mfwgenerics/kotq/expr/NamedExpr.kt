package mfwgenerics.kotq.expr

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Reference<T>
): SelectArgument {
    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.expression(expr, name)
    }
}