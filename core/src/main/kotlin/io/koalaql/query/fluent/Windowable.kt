package io.koalaql.query.fluent

import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.window.LabeledWindow

interface Windowable: UnionableUnionOperand {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): UnionableUnionOperand {
        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody {
            out.windows = windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}