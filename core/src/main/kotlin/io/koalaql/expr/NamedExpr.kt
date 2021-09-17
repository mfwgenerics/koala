package io.koalaql.expr

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Reference<T>
): SelectArgument, AsReference<T> {
    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.expression(expr, name)
    }

    override fun asReference(): Reference<T> = name
}