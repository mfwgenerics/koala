package mfwgenerics.kotq.expr

fun <T : Any> max(aggregatable: Aggregatable<T>): FilterableExpr<T> =
    GroupedOperationExpr(GroupedOperationType.MAX, listOf(aggregatable.buildAggregatable()))