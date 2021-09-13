package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.fluent.SelectedJustUnionOperand
import mfwgenerics.kotq.query.fluent.SelectedUnionOperand

fun select(vararg references: SelectArgument): SelectedUnionOperand =
    Tableless.select(*references)

fun <T : Any> selectJust(labeled: SelectedExpr<T>): SelectedJustUnionOperand<T> =
    Tableless.selectJust(labeled)

fun <T : Any> selectJust(reference: Reference<T>): SelectedJustUnionOperand<T> =
    Tableless.selectJust(reference)