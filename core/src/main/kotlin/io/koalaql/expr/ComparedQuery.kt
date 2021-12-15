package io.koalaql.expr

import io.koalaql.query.built.BuiltSubquery

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltSubquery
): ComparisonOperand<T> {
}