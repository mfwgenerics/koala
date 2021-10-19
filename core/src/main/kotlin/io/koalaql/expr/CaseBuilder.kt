package io.koalaql.expr

interface CaseBuilder {
    fun BuiltCaseExpr<*>.buildIntoCase(): CaseBuilder?
}