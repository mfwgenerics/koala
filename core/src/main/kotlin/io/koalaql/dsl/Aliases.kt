package io.koalaql.dsl

import io.koalaql.IdentifierName
import io.koalaql.query.Alias

fun alias(identifier: String? = null): Alias =
    Alias(IdentifierName(identifier))