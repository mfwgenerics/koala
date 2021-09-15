package mfwgenerics.kotq.query

interface WithOperand {
    fun buildCtedQueryable(): CtedQueryable
}