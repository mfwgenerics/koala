package io.koalaql.values

import io.koalaql.expr.Reference

fun <T> emptyRowSequence(columns: List<Reference<*>>) = object : RowSequence<T> {
    override val columns: List<Reference<*>> = columns

    override fun rowIterator(): RowIterator<T> = object : RowIterator<T> {
        override val row: T get() = error("empty row iterator")
        override fun takeRow(): T = row

        override fun next(): Boolean = false

        override fun close() { }
    }
}