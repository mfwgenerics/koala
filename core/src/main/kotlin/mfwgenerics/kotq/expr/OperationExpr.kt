package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler

class OperationExpr<T : Any>(
    val type: OperationType,
    val args: Collection<QuasiExpr>
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.operation(emitParens, type, args)
    }
}