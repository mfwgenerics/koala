package io.koalaql.query.fluent

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperationType
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder

interface Unionable<out T>: WithableQueryable<T> {
    private class SetOperation(
        val of: Unionable<*>,
        val operand: QueryableUnionOperand<*>,
        val type: SetOperationType,
        val distinctness: Distinctness
    ): UnionedOrderable {
        override fun BuiltQuery.buildInto(): QueryBuilder? {
            with (operand) { buildIntoQueryTail(type, distinctness) }

            return of
        }
    }

    fun union(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.DISTINCT)
    fun unionAll(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.ALL)

    fun intersect(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.DISTINCT)
    fun intersectAll(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.ALL)

    fun except(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.DISTINCT)
    fun exceptAll(against: QueryableUnionOperand<*>): UnionedOrderable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.ALL)
}