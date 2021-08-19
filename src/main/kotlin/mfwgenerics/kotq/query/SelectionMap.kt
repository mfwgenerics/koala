package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.AliasedName

class LabelList(
    val values: List<AliasedName<*>>
) {
    private val positions = hashMapOf<AliasedName<*>, Int>()

    init {
        values.forEachIndexed { ix, it ->
            check (positions.putIfAbsent(it, ix) == null)
                { "duplicate label $it" }
        }
    }
}