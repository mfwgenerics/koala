package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.query.OnConflictAction
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltInsert

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

    fun onConflictUpdate(vararg assignments: Assignment<*>): Returningable =
        OnConflict(this, OnConflictAction.Update(assignments.asList()))
}