package io.koalaql.query.fluent

import io.koalaql.query.LockMode
import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody

interface Lockable: Selectable {
    private class LockQuery(
        val of: Lockable,
        val mode: LockMode
    ): Selectable {
        override fun BuiltQueryBody.buildInto(): BuildsIntoQueryBody? {
            locking = mode

            return of
        }
    }

    fun forUpdate(): Selectable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Selectable = LockQuery(this, LockMode.SHARE)
}