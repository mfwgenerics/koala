package io.koalaql.expr

interface SelectArgument {
    fun MutableSet<Reference<*>>.enforceUniqueReference()
    fun SelectionBuilder.buildIntoSelection()
}