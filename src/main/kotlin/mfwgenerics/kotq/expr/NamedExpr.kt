package mfwgenerics.kotq.expr

sealed interface Labeled<T : Any>: NamedExprs {
    override fun namedExprs(): List<Labeled<*>> = listOf(this)
}

data class LabeledExpr<T : Any>(
    val expr: Expr<T>,
    val name: AliasedName<T>
): Labeled<T>, NamedExprs

data class LabeledName<T : Any>(
    val name: AliasedName<T>
): Labeled<T>

infix fun <T : Any> Expr<T>.named(name: Name<T>): LabeledExpr<T> =
    LabeledExpr(this, name.buildAliased())