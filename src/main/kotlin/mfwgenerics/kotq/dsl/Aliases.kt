package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.query.AliasedQueryable
import mfwgenerics.kotq.query.Subqueryable

fun alias(identifier: String? = null): Alias =
    Alias(IdentifierName(identifier))