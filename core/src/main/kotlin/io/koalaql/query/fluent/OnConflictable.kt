package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.built.BuiltNamedIndex
import io.koalaql.expr.Expr
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

    private fun onConflict(key: OnConflictKey): OnConflictedWhereable {
        return object : OnConflictedWhereable {
            override fun ignore(): GeneratingKeys =
                OnConflict(this@OnConflictable, OnConflictIgnore(key))

            override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
                OnConflict(this@OnConflictable, OnConflictUpdate(key, assignments))

            override fun where(where: Expr<Boolean>) = object : OnConflicted {
                override fun ignore(): GeneratingKeys =
                    OnConflict(this@OnConflictable, OnConflictIgnore(key))

                override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
                    OnConflict(this@OnConflictable, OnConflictUpdate(key, assignments))
            }
        }
    }
    
    private fun onConflict(
        key: () -> OnConflictKey
    ): OnConflicted = object : OnConflicted {
        override fun ignore(): GeneratingKeys =
            OnConflict(this@OnConflictable, OnConflictIgnore(key()))

        override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
            OnConflict(this@OnConflictable, OnConflictUpdate(key(), assignments))
    }
    
    fun onConflict(index: BuiltNamedIndex): OnConflicted = onConflict { 
        OnConflictKeyIndex(index)
    }

    fun onConflict(vararg columns: TableColumn<*>): OnConflictedWhereable {
        return object : OnConflictedWhereable, OnConflicted by onConflict({
            OnConflictKeyColumns(columns.asList(), null)
        }) {
            override fun where(where: Expr<Boolean>) = onConflict {
                OnConflictKeyColumns(columns.asList(), where)
            }
        }
    }

    fun onDuplicate(): OnDuplicated = object : OnDuplicated {
        override fun update(assignments: List<Assignment<*>>): GeneratingKeys =
            OnConflict(this@OnConflictable, OnDuplicateUpdate(assignments))
    }
}