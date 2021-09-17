package io.koalaql.expr

interface SelectArgument {
    fun buildIntoSelection(selection: SelectionBuilder)
}