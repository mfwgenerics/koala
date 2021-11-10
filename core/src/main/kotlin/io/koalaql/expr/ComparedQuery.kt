package io.koalaql.expr

import io.koalaql.query.built.BuiltFullQuery

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltFullQuery
): ComparisonOperand<T> {
}