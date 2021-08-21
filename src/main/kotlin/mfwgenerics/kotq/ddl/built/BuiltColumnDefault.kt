package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.expr.Expr

sealed interface BuiltColumnDefault

class ColumnDefaultValue(
    val value: Any?
): BuiltColumnDefault

class ColumnDefaultExpr(
    val expr: Expr<*>?
): BuiltColumnDefault