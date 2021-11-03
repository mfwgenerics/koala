package io.koalaql.expr

import io.koalaql.IdentifierName
import kotlin.reflect.KClass

class Label<T : Any>(
    type: KClass<T>,
    identifier: IdentifierName
): NamedReference<T>(type, identifier) {
    override fun toString(): String = identifier.asString
        ?.let { "name(\"$it\")" }
        ?: "name"
}