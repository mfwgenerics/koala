package mfwgenerics.kotq.expr

import mfwgenerics.kotq.expr.built.BuildsIntoAggregatedExpr

interface AggregatedExpr<T : Any>: Expr<T>, BuildsIntoAggregatedExpr {
}