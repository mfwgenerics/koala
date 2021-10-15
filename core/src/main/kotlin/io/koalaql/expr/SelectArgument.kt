package io.koalaql.expr

interface SelectArgument {
    fun SelectionBuilder.buildIntoSelection()
}