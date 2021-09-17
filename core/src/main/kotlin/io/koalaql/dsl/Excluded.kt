package io.koalaql.dsl

import io.koalaql.expr.AliasedReference
import io.koalaql.expr.AsReference
import io.koalaql.expr.EXCLUDED_MARKER_ALIAS
import io.koalaql.query.GetsAliasedReference

object Excluded: GetsAliasedReference {
    override fun <T : Any> get(reference: AsReference<T>): AliasedReference<T> =
        EXCLUDED_MARKER_ALIAS[reference]
}