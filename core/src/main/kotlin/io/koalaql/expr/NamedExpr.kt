package io.koalaql.expr

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Reference<T>
): SelectArgument, AsReference<T> {
    override fun SelectionBuilder.buildIntoSelection() {
        expression(expr, name)
    }

    override fun asReference(): Reference<T> = name
}