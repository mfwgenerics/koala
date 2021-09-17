package io.koalaql.query

import io.koalaql.expr.AliasedReference
import io.koalaql.expr.AsReference

interface GetsAliasedReference {
    operator fun <T : Any> get(reference: AsReference<T>): AliasedReference<T>
}