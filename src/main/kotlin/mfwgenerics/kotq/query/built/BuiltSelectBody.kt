package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.window.LabeledWindow

data class BuiltSelectBody(
    val where: BuiltWhere = BuiltWhere(),

    var groupBy: List<Expr<*>> = arrayListOf(),
    var having: Expr<Boolean>? = null,

    var windows: List<LabeledWindow> = emptyList()
) {
    fun populateScope(scope: Scope) {
        where.populateScope(scope)
    }
}