package io.koalaql.query.fluent

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType
import io.koalaql.query.built.BuiltQuery

interface QueryableUnionOperand<out T>: Unionable<T> {
    fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness)
}