package io.koalaql.expr

import io.koalaql.unfoldBuilder

class BuiltCaseExpr<T : Any>: Expr<T> {
    var onExpr: Expr<*>? = null

    var whens: List<WhenThen<*, *>> = emptyList()
    var elseExpr: Expr<*>? = null

    companion object {
        fun <T : Any> from(builder: CaseBuilder): BuiltCaseExpr<T> =
            unfoldBuilder(builder, BuiltCaseExpr()) { it.buildIntoCase() }
    }
}