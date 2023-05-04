package io.koalaql.expr

import io.koalaql.identifier.LabelIdentifier
import kotlin.reflect.KType

class Label<T : Any>(
    type: KType,
    identifier: LabelIdentifier
): NamedReference<T>(type, identifier) {
    override fun toString(): String {
        return "label of `$identifier`, $type"
    }
}