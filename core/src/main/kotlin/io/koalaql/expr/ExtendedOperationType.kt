package io.koalaql.expr

class ExtendedOperationType(
    override val sql: String,
    override val fixity: OperationFixity
): OperationType