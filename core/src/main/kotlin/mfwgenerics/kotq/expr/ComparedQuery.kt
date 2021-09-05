package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler
import mfwgenerics.kotq.query.built.BuiltSubquery

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltSubquery
): ComparisonOperand<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        with (compiler) { compared(emitParens, type, subquery) }
    }
}