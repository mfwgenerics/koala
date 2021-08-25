package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.StandardSql

enum class OperationType(
    override val sql: String,
    val fixity: OperationFixity
): StandardSql {
    EQ("=", OperationFixity.INFIX),
    NEQ("!=", OperationFixity.INFIX),
    LT("<", OperationFixity.INFIX),
    LTE("<=", OperationFixity.INFIX),
    GT(">", OperationFixity.INFIX),
    GTE(">=", OperationFixity.INFIX),

    AND("AND", OperationFixity.INFIX),
    OR("OR", OperationFixity.INFIX),
    NOT("NOT", OperationFixity.INFIX),

    IN("IN", OperationFixity.INFIX),
    NOT_IN("NOT IN", OperationFixity.INFIX),

    IS_NULL("IS NULL", OperationFixity.POSTFIX),
    IS_NOT_NULL("IS NOT NULL", OperationFixity.POSTFIX),

    EXISTS("EXISTS", OperationFixity.PREFIX),
    NOT_EXISTS("NOT EXISTS", OperationFixity.PREFIX),

    DIVIDE("/", OperationFixity.INFIX);

    operator fun <T : Any> invoke(vararg args: QuasiExpr): OperationExpr<T> =
        OperationExpr(this, args.toList())
}