package mfwgenerics.kotq.expr

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.dialect.ExpressionCompiler

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: DataType<T>
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.cast(emitParens, of, type)
    }
}