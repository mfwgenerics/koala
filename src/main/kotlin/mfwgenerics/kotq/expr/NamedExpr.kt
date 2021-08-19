package mfwgenerics.kotq.expr

data class Labeled<T : Any>(
    val expr: Expr<T>,
    val name: AliasedName<T>
): NamedExprs {
    override fun namedExprs(): List<Labeled<*>> = listOf(this)
}

infix fun <T : Any> Expr<T>.`as`(reference: Reference<T>): Labeled<T> =
    Labeled(this, reference.buildAliased())