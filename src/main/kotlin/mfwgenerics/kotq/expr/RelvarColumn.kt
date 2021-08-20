package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import kotlin.reflect.KClass

abstract class RelvarColumn<T : Any>(
    val symbol: String,
    override val type: KClass<T>,
    override val identifier: IdentifierName
): NamedReference<T>(
    type,
    identifier
)