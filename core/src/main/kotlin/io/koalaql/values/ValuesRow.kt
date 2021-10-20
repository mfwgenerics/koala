package io.koalaql.values

import io.koalaql.expr.Expr
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

interface ValuesRow {
    val columns: LabelList

    operator fun <T : Any> get(reference: Reference<T>): Expr<T>
}