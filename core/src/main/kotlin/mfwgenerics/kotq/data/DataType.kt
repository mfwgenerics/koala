package mfwgenerics.kotq.data

import mfwgenerics.kotq.sql.StandardSql
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

sealed class DataType<T : Any>(
    override val type: KClass<T>
): MappedDataType<T, T> {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    override val dataType: DataType<T> get() = this

    override fun convert(from: T): T = from
    override fun unconvert(from: T): T = from
}

sealed class PrimitiveDataType<T : Any>(
    type: KClass<T>
): DataType<T>(type) {
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

class VARCHAR(
    val maxLength: Int
): DataType<String>(String::class) {
    override fun equals(other: Any?): Boolean = other is VARCHAR && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()

    override fun toString(): String = "VARCHAR($maxLength)"
}

class VARBINARY(
    val maxLength: Int
): DataType<ByteArray>(ByteArray::class) {
    override fun equals(other: Any?): Boolean = other is VARBINARY && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()
}

class DECIMAL(
    val precision: Int,
    val scale: Int
): DataType<BigDecimal>(
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
): DataType<T>(type), StandardSql {
    companion object {
        inline operator fun <reified T : Any> invoke(sql: String) = RAW(T::class, sql)
    }

    override fun equals(other: Any?): Boolean = other is RAW<*>
        && type == other.type
        && sql == other.sql

    override fun hashCode(): Int = sql.hashCode()
}

