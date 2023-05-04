package io.koalaql.expr

import io.koalaql.sql.StandardSql

sealed interface OperationType: StandardSql {
    override val sql: String
    val fixity: OperationFixity

    operator fun <T : Any> invoke(vararg args: QuasiExpr): OperationExpr<T> =
        OperationExpr(this, args.toList())
}