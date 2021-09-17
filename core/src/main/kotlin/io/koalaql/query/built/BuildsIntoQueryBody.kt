package io.koalaql.query.built

import io.koalaql.unfoldBuilder

interface BuildsIntoQueryBody {
    fun buildQueryBody(): BuiltQueryBody =
        unfoldBuilder(BuiltQueryBody()) { buildIntoQueryBody(it) }

    fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody?
}