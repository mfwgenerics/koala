package io.koalaql.expr

import io.koalaql.identifier.LabelIdentifier
import kotlin.reflect.KClass

abstract class Column<T : Any>(
    val symbol: String,
    override val type: KClass<T>,
    override val identifier: LabelIdentifier
): NamedReference<T>(
    type,
    identifier
) {
    override fun toString(): String = "`$symbol`"
}