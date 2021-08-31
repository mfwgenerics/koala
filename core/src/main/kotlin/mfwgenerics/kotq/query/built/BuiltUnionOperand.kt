package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.SelectedExpr

interface BuiltUnionOperand {
    fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery
}

