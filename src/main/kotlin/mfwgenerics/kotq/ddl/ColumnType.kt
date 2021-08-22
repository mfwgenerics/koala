package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.ddl.built.BuildsIntoColumnDef
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.fluent.ColumnIncrementable
import mfwgenerics.kotq.sql.StandardSql
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

sealed interface ColumnType<T : Any>: ColumnIncrementable<T> {
    val baseDataType: DataType<*>

    val type: KClass<T>

    fun equivalentKind(other: DataType<*>): Boolean

    fun equivalentKind(other: ColumnType<*>): Boolean =
        equivalentKind(baseDataType)

    override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef? {
        out.columnType = this
        return null
    }
}

class MappedColumnType<F : Any, T : Any>(
    override val type: KClass<T>,
    override val baseDataType: DataType<F>,
    val converter: TypeConverter<F, T>
): ColumnType<T> {
    override fun equivalentKind(other: DataType<*>): Boolean =
        baseDataType.equivalentKind(other)
}

sealed class DataType<T : Any>(
    override val type: KClass<T>
): ColumnType<T> {
    override val baseDataType: DataType<*> get() = this

    override fun equivalentKind(other: DataType<*>): Boolean {
        return this::class == other::class
    }

    object FLOAT : DataType<Float>(Float::class)
    object DOUBLE : DataType<Double>(Double::class)

    class DECIMAL(
        val precision: Int,
        val scale: Int
    ): DataType<BigDecimal>(
        BigDecimal::class
    )

    object INT8: DataType<Byte>(Byte::class)
    object UINT8: DataType<UByte>(UByte::class)

    object INT16: DataType<Short>(Short::class)
    object UINT16: DataType<UShort>(UShort::class)

    object INT32: DataType<Int>(Int::class)
    object UINT32: DataType<UInt>(UInt::class)

    object INT64: DataType<Long>(Long::class)
    object UINT64: DataType<ULong>(ULong::class)

    object DATE: DataType<LocalDate>(LocalDate::class)
    object DATETIME: DataType<LocalDateTime>(LocalDateTime::class)
    object TIME: DataType<LocalTime>(LocalTime::class)

    object INSTANT: DataType<Instant>(Instant::class)

    class VARCHAR(
        val maxLength: Int
    ): DataType<String>(String::class)

    class VARBINARY(
        val maxLength: Int
    ): DataType<ByteArray>(ByteArray::class)

    class RAW<T : Any>(
        type: KClass<T>,
        override val sql: String,
    ): DataType<T>(type), StandardSql {
        companion object {
            inline operator fun <reified T : Any> invoke(sql: String) = RAW(T::class, sql)
        }
    }
}

