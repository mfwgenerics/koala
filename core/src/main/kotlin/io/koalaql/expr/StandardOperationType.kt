package io.koalaql.expr

enum class StandardOperationType(
    override val sql: String,
    override val fixity: OperationFixity
): OperationType {
    NULL("NULL", OperationFixity.NAME),

    EQ("=", OperationFixity.INFIX),
    NEQ("!=", OperationFixity.INFIX),
    LT("<", OperationFixity.INFIX),
    LTE("<=", OperationFixity.INFIX),
    GT(">", OperationFixity.INFIX),
    GTE(">=", OperationFixity.INFIX),

    IS_DISTINCT_FROM("IS DISTINCT FROM", OperationFixity.INFIX),
    IS_NOT_DISTINCT_FROM("IS NOT DISTINCT FROM", OperationFixity.INFIX),

    MOD("%", OperationFixity.INFIX),

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

    LIKE("LIKE", OperationFixity.INFIX),
    NOT_LIKE("NOT LIKE", OperationFixity.INFIX),

    IS_NULL("IS NULL", OperationFixity.POSTFIX),
    IS_NOT_NULL("IS NOT NULL", OperationFixity.POSTFIX),

    EXISTS("EXISTS", OperationFixity.PREFIX),
    NOT_EXISTS("NOT EXISTS", OperationFixity.PREFIX),

    COALESCE("COALESCE", OperationFixity.APPLY),

    CURRENT_TIMESTAMP("CURRENT_TIMESTAMP", OperationFixity.APPLY),

    UPPER("UPPER", OperationFixity.APPLY),
    LOWER("LOWER", OperationFixity.APPLY),

    /* aggregates */
    AVG("AVG", OperationFixity.APPLY),
    COUNT("COUNT", OperationFixity.APPLY),
    MAX("MAX", OperationFixity.APPLY),
    MIN("MIN", OperationFixity.APPLY),
    STDDEV_POP("STDDEV_POP", OperationFixity.APPLY),
    SUM("SUM", OperationFixity.APPLY),
    VAR_POP("VAR_POP", OperationFixity.APPLY),

    /* windows */
    ROW_NUMBER("ROW_NUMBER", OperationFixity.APPLY),
    RANK("RANK", OperationFixity.APPLY),
    DENSE_RANK("DENSE_RANK", OperationFixity.APPLY),
    PERCENT_RANK("PERCENT_RANK", OperationFixity.APPLY),
    CUME_DIST("CUME_DIST", OperationFixity.APPLY),
    NTILE("NTILE", OperationFixity.APPLY),
    LAG("LAG", OperationFixity.APPLY),
    LEAD("LEAD", OperationFixity.APPLY),
    FIRST_VALUE("FIRST_VALUE", OperationFixity.APPLY),
    LAST_VALUE("LAST_VALUE", OperationFixity.APPLY),
    NTH_VALUE("NTH_VALUE", OperationFixity.APPLY);

    operator fun <T : Any> invoke(vararg args: QuasiExpr): OperationExpr<T> =
        OperationExpr(this, args.toList())
}