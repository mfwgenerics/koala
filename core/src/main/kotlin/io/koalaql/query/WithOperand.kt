package io.koalaql.query

interface WithOperand {
    fun buildCtedQueryable(): CtedQueryable
}