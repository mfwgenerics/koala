package io.koalaql.query.fluent

import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.QueryBodyBuilder
import io.koalaql.window.LabeledWindow

interface Windowable: Orderable {
    private class WindowedQuery(
        val lhs: Windowable,
        val windows: List<LabeledWindow>
    ): Orderable {
        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder {
            windows = this@WindowedQuery.windows
            return lhs
        }
    }

    fun window(vararg windows: LabeledWindow): Orderable =
        WindowedQuery(this, windows.asList())
}