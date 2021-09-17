package io.koalaql.expr

import io.koalaql.sql.RawSqlBuilder

class RawExpr<T : Any>(
    val build: RawSqlBuilder.() -> Unit
): Expr<T>