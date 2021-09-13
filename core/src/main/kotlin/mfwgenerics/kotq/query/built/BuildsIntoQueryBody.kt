package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoQueryBody {
    fun buildQueryBody(): BuiltQueryBody =
        unfoldBuilder(BuiltQueryBody()) { buildIntoQueryBody(it) }

    fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody?
}