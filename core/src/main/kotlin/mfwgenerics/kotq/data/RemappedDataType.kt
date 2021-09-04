package mfwgenerics.kotq.data

import kotlin.reflect.KClass

class RemappedDataType<F : Any, T : Any, R : Any>(
    override val type: KClass<R>,
    private val of: MappedDataType<F, T>,
    private val to: (T) -> R,
    private val from: (R) -> T
): MappedDataType<F, R>() {
    override val dataType: DataType<F> = of.dataType

    override fun convert(value: F): R = to(of.convert(value))
    override fun unconvert(value: R): F = of.unconvert(from(value))
}