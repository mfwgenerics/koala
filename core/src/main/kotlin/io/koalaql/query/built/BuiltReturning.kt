package io.koalaql.query.built

import io.koalaql.expr.Reference
import io.koalaql.expr.SelectedExpr
import io.koalaql.sql.Scope

data class BuiltReturning(
    val stmt: BuiltStatement,
    val returned: List<SelectedExpr<*>>
): BuiltSubquery {
    override val columns: List<Reference<*>> = returned.map { it.name }

    override fun columnsUnnamed(): Boolean = false

    override fun populateScope(scope: Scope) { }
}