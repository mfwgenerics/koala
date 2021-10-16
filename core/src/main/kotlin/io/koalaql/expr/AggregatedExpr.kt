package io.koalaql.expr

import io.koalaql.expr.built.AggregatedExprBuilder

interface AggregatedExpr<T : Any>: Expr<T>, AggregatedExprBuilder