package io.koalaql.dsl

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectOperand
import io.koalaql.query.Tableless
import io.koalaql.query.fluent.QueryableOfOneUnionOperand
import io.koalaql.query.fluent.QueryableUnionOperand

fun select(vararg references: SelectArgument): QueryableUnionOperand =
    Tableless.select(*references)

fun <T : Any> select(labeled: SelectOperand<T>): QueryableOfOneUnionOperand<T> =
    Tableless.select(labeled)