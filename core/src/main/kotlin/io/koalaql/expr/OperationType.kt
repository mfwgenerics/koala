package io.koalaql.expr

import io.koalaql.sql.StandardSql

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

    IS_DISTINCT_FROM("IS DISTINCT FROM", OperationFixity.INFIX),
    IS_NOT_DISTINCT_FROM("IS NOT DISTINCT FROM", OperationFixity.INFIX),

    MOD("MOD", OperationFixity.INFIX),

    PLUS("+", OperationFixity.INFIX),
    MINUS("-", OperationFixity.INFIX),
    MULTIPLY("*", OperationFixity.INFIX),
    DIVIDE("/", OperationFixity.INFIX),

    UNARY_MINUS("-", OperationFixity.PREFIX),

    ABS("ABS", OperationFixity.APPLY),
    ACOS("ACOS", OperationFixity.APPLY),
    ASIN("ASIN", OperationFixity.APPLY),
    ATAN("ATAN", OperationFixity.APPLY),
    ATAN2("ATAN2", OperationFixity.APPLY),

    LN("LN", OperationFixity.APPLY),
    LOG("LOG", OperationFixity.APPLY),
    LOG10("LOG10", OperationFixity.APPLY),
    LOG2("LOG2", OperationFixity.APPLY),

    FLOOR("FLOOR", OperationFixity.APPLY),
    CEIL("CEIL", OperationFixity.APPLY),
    ROUND("ROUND", OperationFixity.APPLY),

    SIN("SIN", OperationFixity.APPLY),
    TAN("TAN", OperationFixity.APPLY),
    COS("COS", OperationFixity.APPLY),
    COT("COT", OperationFixity.APPLY),
    EXP("EXP", OperationFixity.APPLY),

    SQRT("SQRT", OperationFixity.APPLY),
    POW("POW", OperationFixity.APPLY),

    AND("AND", OperationFixity.INFIX),
    OR("OR", OperationFixity.INFIX),
    NOT("NOT", OperationFixity.INFIX),

    IN("IN", OperationFixity.INFIX),
    NOT_IN("NOT IN", OperationFixity.INFIX),

    IS_NULL("IS NULL", OperationFixity.POSTFIX),
    IS_NOT_NULL("IS NOT NULL", OperationFixity.POSTFIX),

    EXISTS("EXISTS", OperationFixity.PREFIX),
    NOT_EXISTS("NOT EXISTS", OperationFixity.PREFIX),

    COALESCE("COALESCE", OperationFixity.APPLY),

    CURRENT_TIMESTAMP("CURRENT_TIMESTAMP", OperationFixity.APPLY),

    /* aggregates */
    AVG("AVG", OperationFixity.APPLY),
    COUNT("COUNT", OperationFixity.APPLY),
    MAX("MAX", OperationFixity.APPLY),
    MIN("MIN", OperationFixity.APPLY),
    STDDEV_POP("STDDEV_POP", OperationFixity.APPLY),
    SUM("SUM", OperationFixity.APPLY),
    VAR_POP("VAR_POP", OperationFixity.APPLY);

    operator fun <T : Any> invoke(vararg args: QuasiExpr): OperationExpr<T> =
        OperationExpr(this, args.toList())
}