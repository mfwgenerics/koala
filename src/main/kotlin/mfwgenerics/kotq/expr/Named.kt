package mfwgenerics.kotq.expr

interface Named<T : Any>: Reference<T> {
    val name: Name<T>

    override fun buildIntoAliased(out: AliasedName<T>): Nothing? {
        out.name = this.name
        return null
    }
}