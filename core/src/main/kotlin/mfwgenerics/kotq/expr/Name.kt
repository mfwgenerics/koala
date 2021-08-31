package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

class Name<T : Any>(
    type: KClass<T>,
    identifier: IdentifierName
): NamedReference<T>(type, identifier)