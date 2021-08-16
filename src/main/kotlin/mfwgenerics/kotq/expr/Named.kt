package mfwgenerics.kotq.expr

interface Named<T : Any>: Reference<T> {
    val name: Name<T>

    override fun namedExprs(): List<SelectedExpr<*>> = listOf(
        SelectedExpr(this, name.toAliasedName())
    )

    override fun buildAliasedName(out: AliasedName<T>): Reference<T>? {
        out.name = this.name
        return null
    }
}