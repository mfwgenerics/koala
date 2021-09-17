package io.koalaql.ddl.built

import io.koalaql.expr.Expr

sealed interface BuiltColumnDefault

class ColumnDefaultValue(
    val value: Any?
): BuiltColumnDefault

class ColumnDefaultExpr(
    val expr: Expr<*>
): BuiltColumnDefault