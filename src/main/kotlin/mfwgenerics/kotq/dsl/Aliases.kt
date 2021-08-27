package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.query.Alias

fun alias(identifier: String? = null): Alias =
    Alias(IdentifierName(identifier))