package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.QuasiExpr

interface RawSqlBuilder {
    fun sql(value: String)
    fun expr(expr: QuasiExpr)
}