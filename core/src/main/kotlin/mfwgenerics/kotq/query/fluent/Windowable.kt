package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody
import mfwgenerics.kotq.window.LabeledWindow

interface Windowable: UnionableUnionOperand {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): UnionableUnionOperand {
        override fun buildIntoSelect(out: BuiltQueryBody): BuildsIntoQueryBody {
            out.windows = windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}