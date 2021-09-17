package io.koalaql.query.built

import io.koalaql.expr.SelectedExpr

interface BuiltUnionOperand {
    fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery
}

