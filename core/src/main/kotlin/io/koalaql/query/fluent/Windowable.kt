package io.koalaql.query.fluent

import io.koalaql.query.built.QueryBodyBuilder
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.window.LabeledWindow

interface Windowable: UnionableUnionOperand {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): UnionableUnionOperand {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder {
            windows = this@WindowedQuery.windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}