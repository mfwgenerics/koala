package io.koalaql.query.built

import io.koalaql.expr.Expr
import io.koalaql.query.JoinType
import io.koalaql.sql.Scope

data class BuiltJoin(
    val type: JoinType,
    val to: BuiltRelation,
    val on: Expr<Boolean>?
) {
    fun populateScope(scope: Scope) {
        to.populateScope(scope)
    }
}