package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.dsl.JoinType
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.sql.Scope

data class BuiltJoin(
    val type: JoinType,
    val to: BuiltRelation,
    val on: Expr<Boolean>
) {
    fun populateScope(scope: Scope) {
        to.populateScope(scope)
    }
}