package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody

interface Lockable: Selectable {
    private class LockQuery(
        val of: Lockable,
        val mode: LockMode
    ): Selectable {
        override fun buildIntoSelect(out: BuiltQueryBody): BuildsIntoQueryBody? {
            out.locking = mode

            return of
        }
    }

    fun forUpdate(): Selectable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Selectable = LockQuery(this, LockMode.SHARE)
}