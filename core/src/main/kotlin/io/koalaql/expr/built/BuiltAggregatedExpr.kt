package io.koalaql.expr.built

import io.koalaql.expr.Expr
import io.koalaql.expr.GroupedOperationExpr
import io.koalaql.window.built.BuiltWindow

class BuiltAggregatedExpr {
    lateinit var expr: GroupedOperationExpr<*>

    var filter: Expr<Boolean>? = null
    var over: BuiltWindow? = null
}