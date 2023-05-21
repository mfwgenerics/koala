package io.koalaql.ddl

import io.koalaql.sql.StandardSql
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class UnmappedDataType<T : Any>(
    override val type: KType
): DataType<T, T>() {
    override val mapping: TypeMapping<T, T>? get() = null

    override fun <R : Any> map(mapping: TypeMapping<T, R>): DataType<T, R> =
        MappedDataType(mapping.type, this, mapping)

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    fun defaultRawSql(): String = when (this) {
        DATE -> "DATE"
        is DATETIME -> {
            val suffix = precision?.let { "($precision)" }?:""
            "TIMESTAMP$suffix WITHOUT TIME ZONE"
        }
        is DECIMAL -> "DECIMAL(${precision},${scale})"
        DOUBLE -> "DOUBLE"
        FLOAT -> "FLOAT"
        is TIMESTAMP -> {
            val suffix = precision?.let { "($precision)" }?:""
            "TIMESTAMP$suffix WITH TIME ZONE"
        }
        JSON -> "JSON"
        JSONB -> "JSONB"
        TINYINT -> "TINYINT"
        SMALLINT -> "SMALLINT"
        INTEGER -> "INTEGER"
        BIGINT -> "BIGINT"
        is ExtendedDataType -> sql
        is TIME -> {
            val suffix = precision?.let { "($precision)" }?:""
            "TIME$suffix WITHOUT TIME ZONE"
        }
        is VARBINARY -> "VARBINARY(${maxLength})"
        is VARCHAR -> "VARCHAR(${maxLength})"
        BOOLEAN -> "BOOL"
        TEXT -> "TEXT"
        TINYINT.UNSIGNED -> "TINYINT UNSIGNED"
        SMALLINT.UNSIGNED -> "SMALLINT UNSIGNED"
        INTEGER.UNSIGNED -> "INTEGER UNSIGNED"
        BIGINT.UNSIGNED -> "BIGINT UNSIGNED"
    }

    override fun toString(): String = defaultRawSql()

    override val dataType: UnmappedDataType<T> get() = this
}

sealed class PrimitiveDataType<T : Any>(
    type: KType
): UnmappedDataType<T>(type) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

object FLOAT : PrimitiveDataType<Float>(typeOf<Float>())
object DOUBLE : PrimitiveDataType<Double>(typeOf<Double>())

object TINYINT: PrimitiveDataType<Byte>(typeOf<Byte>()) {
    object UNSIGNED: PrimitiveDataType<UByte>(typeOf<UByte>())
}

object SMALLINT: PrimitiveDataType<Short>(typeOf<Short>()) {
    object UNSIGNED: PrimitiveDataType<UShort>(typeOf<UShort>())
}

object INTEGER: PrimitiveDataType<Int>(typeOf<Int>()) {
    object UNSIGNED: PrimitiveDataType<UInt>(typeOf<UInt>())
}

object BIGINT: PrimitiveDataType<Long>(typeOf<Long>()) {
    object UNSIGNED: PrimitiveDataType<ULong>(typeOf<ULong>())
}

object DATE: PrimitiveDataType<LocalDate>(typeOf<LocalDate>())

open class TIMESTAMP(
    val precision: Int? = null
): UnmappedDataType<Instant>(typeOf<Instant>()) {
    companion object : TIMESTAMP()

    override fun equals(other: Any?): Boolean =
        other is TIMESTAMP && precision == other.precision

    override fun hashCode(): Int = precision.hashCode()
}

object TEXT: PrimitiveDataType<String>(typeOf<String>())
object BOOLEAN: PrimitiveDataType<Boolean>(typeOf<Boolean>())

open class TIME(
    val precision: Int? = null
): UnmappedDataType<LocalTime>(typeOf<LocalTime>()) {
    companion object : TIME()

    override fun equals(other: Any?): Boolean =
        other is TIME && precision == other.precision

    override fun hashCode(): Int = precision.hashCode()
}

open class DATETIME(
    val precision: Int? = null
): UnmappedDataType<LocalDateTime>(typeOf<LocalDateTime>()) {
    companion object : DATETIME()

    override fun equals(other: Any?): Boolean =
        other is DATETIME && precision == other.precision

    override fun hashCode(): Int = precision.hashCode()
}

class VARCHAR(
    val maxLength: Int
): UnmappedDataType<String>(typeOf<String>()) {
    override fun equals(other: Any?): Boolean = other is VARCHAR && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()

    override fun toString(): String = "VARCHAR($maxLength)"
}

class VARBINARY(
    val maxLength: Int
): UnmappedDataType<ByteArray>(typeOf<ByteArray>()) {
    override fun equals(other: Any?): Boolean = other is VARBINARY && maxLength == other.maxLength
    override fun hashCode(): Int = maxLength.hashCode()
}

class DECIMAL(
    val precision: Int,
    val scale: Int
): UnmappedDataType<BigDecimal>(
    typeOf<BigDecimal>()
) {
    override fun equals(other: Any?): Boolean = other is DECIMAL
        && precision == other.precision
        && scale == other.scale

    override fun hashCode(): Int = precision.hashCode() xor scale.hashCode()
}

open class ExtendedDataType<T : Any>(
    type: KType,
    override val sql: String,
): UnmappedDataType<T>(type), StandardSql {
    companion object {
        inline operator fun <reified T : Any> invoke(sql: String) = ExtendedDataType<T>(typeOf<T>(), sql)
    }

    override fun equals(other: Any?): Boolean = other is ExtendedDataType<*>
        && type == other.type
        && sql == other.sql

    override fun hashCode(): Int = sql.hashCode()
}

object JSON: PrimitiveDataType<JsonData>(typeOf<JsonData>())

object JSONB: PrimitiveDataType<JsonData>(typeOf<JsonData>())