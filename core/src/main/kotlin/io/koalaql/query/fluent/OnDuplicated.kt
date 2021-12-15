package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.ExprAssignment
import io.koalaql.dsl.Excluded
import io.koalaql.expr.Column

interface OnDuplicated {
    fun update(assignments: List<Assignment<*>>): GeneratingKeys

    fun update(vararg assignments: Assignment<*>): GeneratingKeys =
        update(assignments.asList())

    /* Syntax sugar for the common case of wanting to update from the inserted values */
    fun set(assignments: List<Column<*>>): GeneratingKeys =
        update(assignments.map {
            @Suppress("unchecked_cast")
            val cast = it as Column<Any>

            ExprAssignment(cast, Excluded[cast])
        })

    fun set(vararg assignments: Column<*>): GeneratingKeys =
        set(assignments.asList())
}