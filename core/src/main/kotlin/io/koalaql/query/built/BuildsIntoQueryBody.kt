package io.koalaql.query.built

import io.koalaql.unfoldBuilder

interface BuildsIntoQueryBody {
    fun buildQueryBody(): BuiltQueryBody =
        unfoldBuilder(BuiltQueryBody()) { it.buildIntoQueryBody() }

    fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody?
}