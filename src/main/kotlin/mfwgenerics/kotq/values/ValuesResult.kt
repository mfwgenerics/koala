package mfwgenerics.kotq.values

import mfwgenerics.kotq.expr.AliasedName
import mfwgenerics.kotq.query.LabelList

interface ValuesResult {
    val labels: LabelList

    fun next(): Boolean

    //operator fun <T : Any> get(label: AliasedName<T>)
}