package io.koalaql.dsl

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectOperand
import io.koalaql.query.Tableless

fun select(vararg references: SelectArgument) =
    Tableless.select(*references)

fun <T : Any> select(labeled: SelectOperand<T>) =
    Tableless.select(labeled)