package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.ExprAssignment
import mfwgenerics.kotq.ddl.built.BuiltNamedIndex
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

    private fun onConflict(keys: List<BuiltNamedIndex>): OnConflicted {
        return object : OnConflicted {
            override fun ignore(): Returningable =
                OnConflict(this@OnConflictable, OnConflictAction.Ignore(keys))

            override fun update(assignments: List<Assignment<*>>): Returningable =
                OnConflict(this@OnConflictable, OnConflictAction.Update(keys, assignments))
        }
    }

    fun onConflict(keys: BuiltNamedIndex): OnConflicted =
        onConflict(listOf(keys))

    fun onDuplicate(): OnConflicted =
        onConflict(emptyList())
}