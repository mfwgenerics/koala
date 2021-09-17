package io.koalaql.sql

interface SqlPrefix {
    fun next(block: () -> Unit)

    fun <T> forEach(values: Iterable<T>, operation: (T) -> Unit) {
        values.forEach { next { operation(it) } }
    }
}