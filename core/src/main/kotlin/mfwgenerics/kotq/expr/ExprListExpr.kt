package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler

class ExprListExpr<T : Any>(
    val exprs: Collection<Expr<T>>
): QuasiExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        with (compiler) { listExpr(emitParens, exprs) }
    }
}