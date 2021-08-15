package mfwgenerics.kotq

import mfwgenerics.kotq.expr.AliasedReference
import mfwgenerics.kotq.expr.Reference

class Alias {
    operator fun <T : Any> get(reference: Reference<T>) = AliasedReference(this, reference)
}