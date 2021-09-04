package mfwgenerics.kotq.data

import mfwgenerics.kotq.expr.Literal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class TypeMappings {
    private val mappings = ConcurrentHashMap<KClass<*>, MappedDataType<*, *>>()

    fun <F : Any, T : Any> register(mapped: MappedDataType<F, T>) {
        val type = mapped.type

        val existing = mappings.putIfAbsent(type, mapped) ?: return

        check (existing.dataType.type == mapped.dataType.type) { "Type mapping for $type already exists but with conflicting base type." +
            " Expected base type: ${mapped.dataType.type}" +
            " Found base type: ${existing.dataType.type}"
        }
    }

    @Suppress("unchecked_cast")
    fun <T : Any> unconvert(literal: Literal<T>): Literal<*> {
        val mapping = (mappings[literal.type] ?: return literal) as MappedDataType<*, T>

        return Literal(
            mapping.dataType.type as KClass<Any>,
            literal.value?.let { mapping.unconvert(it) }
        )
    }

    @Suppress("unchecked_cast")
    fun <T : Any> convert(type: KClass<T>, read: (KClass<*>) -> Any?): T? {
        val mapping = mappings[type] as? MappedDataType<Any, T>
            ?: return read(type) as T?

        return read(type)?.let { mapping.convert(it) }
    }
}