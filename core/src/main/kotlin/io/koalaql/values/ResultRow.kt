package io.koalaql.values

import io.koalaql.ddl.TableColumnNotNull
import io.koalaql.expr.AsReference
import io.koalaql.expr.Reference
import io.koalaql.query.LabelList

abstract class ResultRow {
    abstract val columns: LabelList

    abstract fun <T : Any> getOrNull(reference: Reference<T>): T?

    fun <T : Any> getOrNull(reference: AsReference<T>): T? =
        getOrNull(reference.asReference())

    fun <T : Any> getValue(reference: AsReference<T>): T =
        checkNotNull(getOrNull(reference)) { "expected non-null $reference" }

    operator fun <T : Any> get(reference: AsReference<T>): T? = getOrNull(reference)
    operator fun <T : Any> get(column: TableColumnNotNull<T>): T = getValue(column)

    override fun toString(): String =
        columns.asSequence().map { "$it=${getOrNull(it)}" }.joinToString()
}