package io.koalaql.query

import io.koalaql.expr.Reference

interface LabelList: List<Reference<*>> {
    fun positionOf(reference: Reference<*>): Int?
}