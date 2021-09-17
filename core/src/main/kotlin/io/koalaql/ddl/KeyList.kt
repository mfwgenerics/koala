package io.koalaql.ddl

import io.koalaql.expr.Expr

class KeyList(
    val keys: List<Expr<*>>
)