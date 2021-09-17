package io.koalaql.expr

open class ElseableCaseExpr<T : Any, R : Any>(
    isGeneralCase: Boolean,
    onExpr: Expr<T>,
    cases: List<CaseWhenThen<T, R>>,
    elseExpr: Expr<R>?
): CaseExpr<T, R>(isGeneralCase, onExpr, cases, elseExpr) {
    infix fun else_(expr: Expr<R>): Expr<R> = CaseExpr(isGeneralCase, onExpr, cases, expr)
}