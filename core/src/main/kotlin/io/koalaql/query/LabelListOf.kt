package io.koalaql.query

import io.koalaql.expr.Reference

class LabelListOf(
    private val values: List<Reference<*>>
): LabelList, List<Reference<*>> by values {
    private val positions = hashMapOf<Reference<*>, Int>().also { positions ->
        values.forEachIndexed { ix, it ->
            check (positions.putIfAbsent(it, ix) == null)
            { "duplicate label $it" }
        }
    }

    override fun positionOf(reference: Reference<*>): Int? =
        positions[reference]
}