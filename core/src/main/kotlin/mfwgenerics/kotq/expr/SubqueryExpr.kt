package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler
import mfwgenerics.kotq.query.built.BuiltSubquery

class SubqueryExpr(
    val subquery: BuiltSubquery
): QuasiExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.subquery(emitParens, subquery)
    }
}