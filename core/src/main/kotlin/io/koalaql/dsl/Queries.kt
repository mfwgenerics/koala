package io.koalaql.dsl

import io.koalaql.expr.Reference
import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectedExpr
import io.koalaql.query.Tableless
import io.koalaql.query.fluent.SelectedJustUnionOperand
import io.koalaql.query.fluent.SelectedUnionOperand

fun select(vararg references: SelectArgument): SelectedUnionOperand =
    Tableless.select(*references)

fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJustUnionOperand<T> =
    Tableless.selectJust(labeled)

fun <T : Any> selectJust(reference: Reference<T>): SelectedJustUnionOperand<T> =
    Tableless.selectJust(reference)