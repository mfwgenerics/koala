package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.AsReference
import mfwgenerics.kotq.expr.EXCLUDED_MARKER_ALIAS
import mfwgenerics.kotq.query.GetsAliasedReference

object Excluded: GetsAliasedReference {
    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> =
        EXCLUDED_MARKER_ALIAS[reference]
}