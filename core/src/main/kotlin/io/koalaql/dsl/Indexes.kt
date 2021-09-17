package io.koalaql.dsl

import io.koalaql.ddl.KeyList
import io.koalaql.expr.Expr

fun keys(key0: Expr<*>, vararg keyN: Expr<*>): KeyList =
    KeyList(listOf(key0, *keyN))