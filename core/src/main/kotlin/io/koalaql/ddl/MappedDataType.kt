package io.koalaql.ddl

import io.koalaql.expr.Literal
import kotlin.reflect.KClass

class MappedDataType<F : Any, T : Any>(
    override val type: KClass<T>,
    override val dataType: UnmappedDataType<F>,
    override val mapping: TypeMapping<F, T>
): DataType<F, T>() {
    fun unconvertLiteral(from: Literal<T>) = Literal(
        type = dataType.type,
        value = from.value?.let { mapping.unconvert(it) }
    )

    @Suppress("unchecked_cast")
    fun unconvertLiteralUnchecked(from: Literal<*>) = unconvertLiteral(from as Literal<T>)

    override fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<F, R> =
        MappedDataType(mapping.type, dataType, this.mapping.then(mapping))
}