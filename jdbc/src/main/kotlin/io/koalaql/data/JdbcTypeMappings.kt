package io.koalaql.data

import io.koalaql.ddl.*
import io.koalaql.expr.Reference
import io.koalaql.sql.TypeMappings
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("RemoveExplicitTypeArguments")
class JdbcTypeMappings {
    private val mappings = hashMapOf<KType, JdbcMappedType<*>>()

    private val localDateTime = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(ISO_LOCAL_TIME)
        .toFormatter()

    init {
        register<Boolean>(
            { stmt, index, value -> stmt.setBoolean(index, value) },
            { rs, index -> rs.getBoolean(index).takeUnless { rs.wasNull() } }
        )

        register<Byte>(
            { stmt, index, value -> stmt.setByte(index, value) },
            { rs, index -> rs.getByte(index).takeUnless { rs.wasNull() } }
        )

        register<Short>(
            { stmt, index, value -> stmt.setShort(index, value) },
            { rs, index -> rs.getShort(index).takeUnless { rs.wasNull() } }
        )

        register<Int>(
            { stmt, index, value -> stmt.setInt(index, value) },
            { rs, index -> rs.getInt(index).takeUnless { rs.wasNull() } }
        )

        register<Long>(
            { stmt, index, value -> stmt.setLong(index, value) },
            { rs, index -> rs.getLong(index).takeUnless { rs.wasNull() } }
        )

        register<UByte>(
            { stmt, index, value -> stmt.setShort(index, value.toShort()) },
            { rs, index -> rs.getShort(index).takeUnless { rs.wasNull() }?.toUByte() }
        )

        register<UShort>(
            { stmt, index, value -> stmt.setInt(index, value.toInt()) },
            { rs, index -> rs.getInt(index).takeUnless { rs.wasNull() }?.toUShort() }
        )

        register<UInt>(
            { stmt, index, value -> stmt.setLong(index, value.toLong()) },
            { rs, index -> rs.getLong(index).takeUnless { rs.wasNull() }?.toUInt() }
        )

        register<ULong>(
            { stmt, index, value -> stmt.setBigDecimal(index, BigDecimal(value.toString())) },
            { rs, index -> rs.getBigDecimal(index)?.toLong()?.toULong() }
        )

        register<Float>(
            { stmt, index, value -> stmt.setFloat(index, value) },
            { rs, index -> rs.getFloat(index).takeUnless { rs.wasNull() } }
        )

        register<Double>(
            { stmt, index, value -> stmt.setDouble(index, value) },
            { rs, index -> rs.getDouble(index).takeUnless { rs.wasNull() } }
        )

        register<String>(
            { stmt, index, value -> stmt.setString(index, value) },
            { rs, index -> rs.getString(index) }
        )

        register<BigDecimal>(
            { stmt, index, value -> stmt.setBigDecimal(index, value) },
            { rs, index -> rs.getBigDecimal(index) }
        )

        register<ByteArray>(
            { stmt, index, value -> stmt.setBytes(index, value) },
            { rs, index -> rs.getBytes(index) }
        )

        register<LocalDate>(
            { stmt, index, value -> stmt.setObject(index, value) },
            { rs, index -> rs.getString(index)?.let { LocalDate.parse(it) } }
        )

        register<LocalTime>(
            { stmt, index, value -> stmt.setObject(index, value) },
            { rs, index -> rs.getString(index)?.let { LocalTime.parse(it) } }
        )

        register<LocalDateTime>(
            { stmt, index, value -> stmt.setObject(index, value) },
            { rs, index -> rs.getString(index)?.let { localDateTime.parse(it, LocalDateTime::from) } }
        )
    }

    fun <T : Any> register(type: KType, mapping: JdbcMappedType<T>) {
        mappings[type] = mapping
    }

    inline fun <reified T : Any> register(
        crossinline writeJdbc: (stmt: PreparedStatement, index: Int, value: T) -> Unit,
        crossinline readJdbc: (rs: ResultSet, index: Int) -> T?
    ) = register(typeOf<T>(), object : JdbcMappedType<T> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: T) =
            writeJdbc(stmt, index, value)

        override fun readJdbc(rs: ResultSet, index: Int): T? = readJdbc(rs, index)
    })

    @Suppress("unchecked_cast")
    private fun <T : Any> mappingFor(type: KType): JdbcMappedType<T> =
        checkNotNull(mappings[type]) { "no JDBC mapping for $type" } as JdbcMappedType<T>

    @Suppress("unchecked_cast")
    fun <T : Any> mappingFor(ktype: KType, dataType: DataType<*, T>?): JdbcMappedType<T> =
        when (dataType) {
            is MappedDataType -> {
                dataType as MappedDataType<Any, T>

                mappingFor<Any>(dataType.dataType.type).derive(dataType.mapping)
            }
            else -> mappingFor(ktype)
        }
}