package io.koalaql.query

import io.koalaql.expr.Reference

class InsertableLabelList(
    private val values: MutableList<Reference<*>> = arrayListOf()
): LabelList, List<Reference<*>> by values {
    private val positions = hashMapOf<Reference<*>, Int>().also { positions ->
        values.forEachIndexed { ix, it ->
            check(positions.putIfAbsent(it, ix) == null)
            { "duplicate label $it" }
        }
    }

    override fun positionOf(reference: Reference<*>): Int? =
        positions[reference]

    fun insert(reference: Reference<*>): Int {
        positions.putIfAbsent(reference, positions.size)
            ?.let { return it }

        values.add(reference)
        return values.size - 1
    }

    override fun toString(): String = "$values"
}