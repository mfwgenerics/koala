package mfwgenerics.kotq.expr

abstract class Named<T : Any>: Reference<T> {
    abstract val name: Name<T>

    override fun buildIntoAliased(out: AliasedName<T>): Nothing? {
        out.identifier = this.name
        return null
    }
}