package io.koalaql.query.fluent

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder

interface QueryableUnionOperand<out T>: Unionable<T> {
    override fun BuiltQuery.buildInto(): QueryBuilder?

    fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness)
}