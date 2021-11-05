package io.koalaql.values

import io.koalaql.expr.Reference

object EmptyRow: ResultRow {
    override val columns: List<Reference<*>> = emptyList()

    override fun <T : Any> getOrNull(reference: Reference<T>): T? = null

    override fun toString(): String = "EmptyRow"
}