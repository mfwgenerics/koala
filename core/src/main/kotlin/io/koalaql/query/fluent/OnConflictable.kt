package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex
import io.koalaql.query.OnConflictAction
import io.koalaql.query.built.BuildsIntoInsert
import io.koalaql.query.built.BuiltInsert

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