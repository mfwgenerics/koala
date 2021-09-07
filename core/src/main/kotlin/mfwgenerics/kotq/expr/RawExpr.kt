package mfwgenerics.kotq.expr

import mfwgenerics.kotq.sql.RawSqlBuilder

class RawExpr<T : Any>(
    val build: RawSqlBuilder.() -> Unit
): Expr<T>