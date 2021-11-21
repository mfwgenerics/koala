package io.koalaql.dsl

import io.koalaql.identifier.LabelIdentifier
import io.koalaql.query.Alias

fun alias(identifier: String? = null): Alias =
    Alias(LabelIdentifier(identifier))