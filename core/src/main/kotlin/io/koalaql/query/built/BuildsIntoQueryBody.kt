package io.koalaql.query.built

interface BuildsIntoQueryBody {
    fun BuiltQueryBody.buildInto(): BuildsIntoQueryBody?
}