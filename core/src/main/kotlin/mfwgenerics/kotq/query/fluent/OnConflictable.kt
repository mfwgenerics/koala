package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.ExprAssignment
import mfwgenerics.kotq.dsl.Excluded
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.OnConflictAction
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltInsert
import mfwgenerics.kotq.setTo

interface OnConflictable: Returningable {
    private class OnConflict(
        val lhs: OnConflictable,
        val action: OnConflictAction
    ): Returningable {
        override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert {
            out.onConflict = action
            return lhs
        }
    }

    fun onConflictIgnore(): Returningable =
        OnConflict(this, OnConflictAction.Ignore)

    fun onConflictUpdate(assignments: List<Assignment<*>>): Returningable =
        OnConflict(this, OnConflictAction.Update(assignments))

    fun onConflictUpdate(vararg assignments: Assignment<*>): Returningable =
        onConflictUpdate(assignments.asList())

    /* Syntax sugar for the common case of wanting to update from the inserted values */
    fun onConflictSet(vararg assignments: Reference<*>): Returningable =
        onConflictUpdate(assignments.map {
            @Suppress("unchecked_cast")
            val cast = it as Reference<Any>

            ExprAssignment(cast, Excluded[cast])
        })
}