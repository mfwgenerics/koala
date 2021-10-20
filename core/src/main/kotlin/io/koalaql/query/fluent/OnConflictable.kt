package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex
import io.koalaql.query.*
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder

interface OnConflictable: Returningable {
    private class OnConflict(
        val lhs: OnConflictable,
        val action: OnConflictOrDuplicateAction
    ): Returningable {
        override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
            onConflict = action
            return lhs
        }
    }

    fun onConflict(keys: BuiltNamedIndex): OnConflicted {
        return object : OnConflicted {
            override fun ignore(): Returningable =
                OnConflict(this@OnConflictable, OnConflictIgnore(keys))

            override fun update(assignments: List<Assignment<*>>): Returningable =
                OnConflict(this@OnConflictable, OnConflictUpdate(keys, assignments))
        }
    }

    fun onDuplicate(): OnDuplicated = object : OnDuplicated {
        override fun update(assignments: List<Assignment<*>>): Returningable =
            OnConflict(this@OnConflictable, OnDuplicateUpdate(assignments))
    }
}