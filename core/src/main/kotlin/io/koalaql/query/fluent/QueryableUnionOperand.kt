package io.koalaql.query.fluent

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.built.FullQueryBuilder

interface QueryableUnionOperand<out T>: Unionable<T> {
    override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder?

    fun BuiltFullQuery.buildIntoFullQueryTail(type: SetOperationType, distinctness: Distinctness)
}