package io.koalaql.expr

import io.koalaql.expr.built.BuildsIntoAggregatedExpr

interface AggregatedExpr<T : Any>: Expr<T>, BuildsIntoAggregatedExpr