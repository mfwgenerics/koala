package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex
import io.koalaql.query.*
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder

interface OnConflictable: GeneratingKeys {
    private class OnConflict(
        val lhs: OnConflictable,
        val action: OnConflictOrDuplicateAction
    ): GeneratingKeys {
        override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
            onConflict = action
            return lhs
        }
    }

    fun onConflict(keys: BuiltNamedIndex): OnConflicted {
        return object : OnConflicted {
            override fun ignore(): GeneratingKeys =
                OnConflict(this@OnConflictable, OnConflictIgnore(keys))

            override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
                OnConflict(this@OnConflictable, OnConflictUpdate(keys, assignments))
        }
    }

    fun onDuplicate(): OnDuplicated = object : OnDuplicated {
        override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
            OnConflict(this@OnConflictable, OnDuplicateUpdate(assignments))
    }
}