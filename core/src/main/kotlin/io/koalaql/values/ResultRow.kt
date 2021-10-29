package io.koalaql.values

import io.koalaql.ddl.TableColumnNotNull
import io.koalaql.expr.AsReference
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

interface ResultRow {
    val columns: LabelList

    fun <T : Any> getOrNull(reference: Reference<T>): T?

    fun <T : Any> getOrNull(reference: AsReference<T>): T? =
        getOrNull(reference.asReference())

    fun <T : Any> getValue(reference: AsReference<T>): T =
        checkNotNull(getOrNull(reference)) {
            if (columns.positionOf(reference.asReference()) != null) {
                "expected non-null $reference. did you mean to use getOrNull?"
            } else {
                "expected column $reference. is $reference missing from select?"
            }
        }

    operator fun <T : Any> get(reference: AsReference<T>): T? = getOrNull(reference)
    operator fun <T : Any> get(column: TableColumnNotNull<T>): T = getValue(column)

    override fun toString(): String
}