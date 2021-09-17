package io.koalaql.sql

import io.koalaql.expr.QuasiExpr

interface RawSqlBuilder {
    fun sql(value: String)
    fun expr(expr: QuasiExpr)
}