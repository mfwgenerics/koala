package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.ddl.KeyList
import mfwgenerics.kotq.expr.Expr

fun keys(key0: Expr<*>, vararg keyN: Expr<*>): KeyList =
    KeyList(listOf(key0, *keyN))