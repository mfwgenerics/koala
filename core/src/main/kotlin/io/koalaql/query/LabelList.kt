package io.koalaql.query

import io.koalaql.expr.Reference

class LabelList(
    val values: List<Reference<*>>,
    private val positions: Map<Reference<*>, Int>
) {
    constructor(values: List<Reference<*>>): this(
        values,
        hashMapOf<Reference<*>, Int>().also { positions ->
            values.forEachIndexed { ix, it ->
                check (positions.putIfAbsent(it, ix) == null)
                { "duplicate label $it" }
            }
        }
    )

    fun positionOf(reference: Reference<*>): Int? =
        positions[reference]
}