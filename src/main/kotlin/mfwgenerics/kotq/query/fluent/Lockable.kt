package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery

interface Lockable: Selectable {
    private class LockQuery(
        val of: Lockable,
        val mode: LockMode
    ): Selectable {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.locking = mode

            return of
        }
    }

    fun forUpdate(): Selectable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Selectable = LockQuery(this, LockMode.SHARE)
}