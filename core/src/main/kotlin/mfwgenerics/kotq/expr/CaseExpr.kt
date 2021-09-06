package mfwgenerics.kotq.expr

open class CaseExpr<T : Any, R : Any>(
    val isGeneralCase: Boolean,
    val onExpr: Expr<T>,
    val cases: List<CaseWhenThen<T, R>>,
    val elseExpr: Expr<R>?
): Expr<R>
