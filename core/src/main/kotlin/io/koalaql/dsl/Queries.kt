package io.koalaql.dsl

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectOperand
import io.koalaql.query.Tableless
import io.koalaql.query.fluent.SelectedJustUnionOperand
import io.koalaql.query.fluent.SelectedUnionOperand

fun select(vararg references: SelectArgument): SelectedUnionOperand =
    Tableless.select(*references)

fun <T : Any> select(labeled: SelectOperand<T>): SelectedJustUnionOperand<T> =
    Tableless.select(labeled)