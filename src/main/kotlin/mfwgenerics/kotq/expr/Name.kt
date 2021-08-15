package mfwgenerics.kotq.expr

class Name<T : Any>: Named<T> {
    override val name: Name<T> get() = this
}