package io.koalaql.values

import io.koalaql.expr.Reference

interface RowWriter {
    operator fun <T : Any> set(reference: Reference<T>, value: T?)
}