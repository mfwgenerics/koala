package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoQueryBody {
    fun buildSelect(): BuiltQueryBody =
        unfoldBuilder(BuiltQueryBody()) { buildIntoSelect(it) }

    fun buildIntoSelect(out: BuiltQueryBody): BuildsIntoQueryBody?
}