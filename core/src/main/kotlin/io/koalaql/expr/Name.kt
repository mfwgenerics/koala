package io.koalaql.expr

import io.koalaql.IdentifierName
import kotlin.reflect.KClass

class Name<T : Any>(
    type: KClass<T>,
    identifier: IdentifierName
): NamedReference<T>(type, identifier)