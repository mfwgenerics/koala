package io.koalaql.data

import io.koalaql.sql.StandardSql
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

sealed class UnmappedDataType<T : Any>(
    override val type: KClass<T>
): DataType<T, T>() {
    override val mapping: TypeMapping<T, T>? get() = null

    override fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<T, R> =
        MappedDataType(mapping.type, this, mapping)

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    override val dataType: UnmappedDataType<T> get() = this
}

sealed class PrimitiveDataType<T : Any>(
    type: KClass<T>
): UnmappedDataType<T>(type) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

object FLOAT : PrimitiveDataType<Float>(Float::class)
object DOUBLE : PrimitiveDataType<Double>(Double::class)

object TINYINT: PrimitiveDataType<Byte>(Byte::class) {
    object UNSIGNED: PrimitiveDataType<UByte>(UByte::class)
}

object SMALLINT: PrimitiveDataType<Short>(Short::class) {
    object UNSIGNED: PrimitiveDataType<UShort>(UShort::class)
}

object INTEGER: PrimitiveDataType<Int>(Int::class) {
    object UNSIGNED: PrimitiveDataType<UInt>(UInt::class)
}

object BIGINT: PrimitiveDataType<Long>(Long::class) {
    object UNSIGNED: PrimitiveDataType<ULong>(ULong::class)
}

object DATE: PrimitiveDataType<LocalDate>(LocalDate::class)
object DATETIME: PrimitiveDataType<LocalDateTime>(LocalDateTime::class)
object TIME: PrimitiveDataType<LocalTime>(LocalTime::class)

object INSTANT: PrimitiveDataType<Instant>(Instant::class)

object TEXT: PrimitiveDataType<String>(String::class)
object BOOLEAN: PrimitiveDataType<Boolean>(Boolean::class)

class VARCHAR(
    val maxLength: Int
): UnmappedDataType<String>(String::class) {
    override fun equals(other: Any?): Boolean = other is VARCHAR && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()

    override fun toString(): String = "VARCHAR($maxLength)"
}

class VARBINARY(
    val maxLength: Int
): UnmappedDataType<ByteArray>(ByteArray::class) {
    override fun equals(other: Any?): Boolean = other is VARBINARY && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()
}

class DECIMAL(
    val precision: Int,
    val scale: Int
): UnmappedDataType<BigDecimal>(
    BigDecimal::class
) {
    override fun equals(other: Any?): Boolean = other is DECIMAL
        && precision == other.precision
        && scale == other.scale

    override fun hashCode(): Int = precision.hashCode() xor scale.hashCode()
}

class RAW<T : Any>(
    type: KClass<T>,
    override val sql: String,
): UnmappedDataType<T>(type), StandardSql {
    companion object {
        inline operator fun <reified T : Any> invoke(sql: String) = RAW(T::class, sql)
    }

    override fun equals(other: Any?): Boolean = other is RAW<*>
        && type == other.type
        && sql == other.sql

    override fun hashCode(): Int = sql.hashCode()
}

