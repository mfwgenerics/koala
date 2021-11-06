package io.koalaql.expr

import io.koalaql.IdentifierName
import kotlin.reflect.KClass

class Label<T : Any>(
    type: KClass<T>,
    identifier: IdentifierName
): NamedReference<T>(type, identifier) {
    override fun toString(): String {
        val ident = identifier.asString
            ?.let { "\"$it\"" }
            ?:"..."

        return "label_of_${type.simpleName?.lowercase()}($ident)"
    }
}