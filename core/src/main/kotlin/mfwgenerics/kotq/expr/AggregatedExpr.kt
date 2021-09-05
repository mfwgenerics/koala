package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler
import mfwgenerics.kotq.expr.built.BuildsIntoAggregatedExpr

interface AggregatedExpr<T : Any>: Expr<T>, BuildsIntoAggregatedExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.aggregated(emitParens, buildAggregated())
    }
}