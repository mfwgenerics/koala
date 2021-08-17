package mfwgenerics.kotq.expr

interface NamedExprs {
    fun namedExprs(): List<Labeled<*>>
}