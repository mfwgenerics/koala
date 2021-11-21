package io.koalaql.expr

import io.koalaql.identifier.LabelIdentifier
import kotlin.reflect.KClass

class Label<T : Any>(
    type: KClass<T>,
    identifier: LabelIdentifier
): NamedReference<T>(type, identifier) {
    override fun toString(): String {
        return "label_of_${type.simpleName?.lowercase()}($identifier)"
    }
}