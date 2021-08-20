package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.Reference

class LabelList(
    val values: List<Reference<*>>
) {
    private val positions = hashMapOf<Reference<*>, Int>()

    init {
        values.forEachIndexed { ix, it ->
            check (positions.putIfAbsent(it, ix) == null)
                { "duplicate label $it" }
        }
    }

    fun positionOf(reference: Reference<*>): Int? =
        positions[reference]
}