package mfwgenerics.kotq.expr.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.GroupedOperationExpr
import mfwgenerics.kotq.window.built.BuiltWindow

class BuiltAggregatedExpr {
    lateinit var expr: GroupedOperationExpr<*>

    var filter: Expr<Boolean>? = null
    var over: BuiltWindow? = null
}