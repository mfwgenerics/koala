package mfwgenerics.kotq.expr

import mfwgenerics.kotq.window.BuiltWindow

class BuiltAggregatedExpr {
    lateinit var expr: GroupedOperationExpr<*>

    var filter: Expr<Boolean>? = null
    var over: BuiltWindow? = null
}