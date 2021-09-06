package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.built.BuiltSubquery

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltSubquery
): ComparisonOperand<T> {
}