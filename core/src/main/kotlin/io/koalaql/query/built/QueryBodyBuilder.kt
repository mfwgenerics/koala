package io.koalaql.query.built

interface QueryBodyBuilder {
    fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder?
}