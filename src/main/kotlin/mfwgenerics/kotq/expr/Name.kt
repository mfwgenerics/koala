package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

class Name<T : Any>(
    type: KClass<T>,
    identifier: IdentifierName
): Named<T>(type, identifier)