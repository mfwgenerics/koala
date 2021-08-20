package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.built.BuildsIntoSelectBody
import mfwgenerics.kotq.query.built.BuiltSelectBody
import mfwgenerics.kotq.window.LabeledWindow

interface Windowable: UnionableUnionOperand {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): UnionableUnionOperand {
        override fun buildIntoSelectBody(out: BuiltSelectBody): BuildsIntoSelectBody? {
            out.windows = windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}