package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.window.LabeledWindow

interface Windowable: UnionableUnionOperand {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): UnionableUnionOperand {
        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.windows = windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}