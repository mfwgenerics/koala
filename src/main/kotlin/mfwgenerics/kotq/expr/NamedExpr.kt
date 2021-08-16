package mfwgenerics.kotq.expr

data class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: AliasedName<T>
): NamedExprs {
    override fun namedExprs(): List<SelectedExpr<*>> = listOf(this)
}

infix fun <T : Any> Expr<T>.named(name: Name<T>): SelectedExpr<T> =
    SelectedExpr(this, name.toAliasedName())