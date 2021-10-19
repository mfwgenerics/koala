package io.koalaql.expr.fluent

import io.koalaql.expr.BuiltCaseExpr
import io.koalaql.expr.CaseBuilder
import io.koalaql.expr.Expr

interface EndableCase<T : Any>: CaseBuilder {
    fun end(): Expr<T> = BuiltCaseExpr.from(this)
}