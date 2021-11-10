package io.koalaql.expr

import io.koalaql.query.built.BuiltQuery

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltQuery
): ComparisonOperand<T> {
}