package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.ExprAssignment
import mfwgenerics.kotq.dsl.Excluded
import mfwgenerics.kotq.expr.Reference

interface OnConflicted {
    fun ignore(): Returningable
    fun update(assignments: List<Assignment<*>>): Returningable

    fun update(vararg assignments: Assignment<*>): Returningable =
        update(assignments.asList())

    /* Syntax sugar for the common case of wanting to update from the inserted values */
    fun set(vararg assignments: Reference<*>): Returningable =
        update(assignments.map {
            @Suppress("unchecked_cast")
            val cast = it as Reference<Any>

            ExprAssignment(cast, Excluded[cast])
        })
}