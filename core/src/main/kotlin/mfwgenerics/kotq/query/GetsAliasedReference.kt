package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.AsReference

interface GetsAliasedReference {
    operator fun <T : Any> get(reference: AsReference<T>): AliasedReference<T>
}