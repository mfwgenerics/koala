package io.koalaql.query.fluent

import io.koalaql.query.LockMode
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.QueryBodyBuilder

interface Lockable: Selectable {
    private class LockQuery(
        val of: Lockable,
        val mode: LockMode
    ): Selectable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder? {
            locking = mode

            return of
        }
    }

    fun forUpdate(): Selectable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Selectable = LockQuery(this, LockMode.SHARE)
}