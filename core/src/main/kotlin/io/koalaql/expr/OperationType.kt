package io.koalaql.expr

import io.koalaql.sql.StandardSql

sealed interface OperationType: StandardSql {
    override val sql: String
    val fixity: OperationFixity
}