package io.koalaql.data

import io.koalaql.ddl.DataType
import io.koalaql.ddl.TypeMapping
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("RemoveExplicitTypeArguments")
class JdbcTypeMappings {
    private val mappings = ConcurrentHashMap<KClass<*>, JdbcMappedType<*>>()

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

    fun <T : Any> register(type: KClass<T>, mapping: JdbcMappedType<T>) {
        mappings[type] = mapping
    }

    fun <F : Any, T : Any> register(from: KClass<F>, mapped: TypeMapping<F, T>) {
        val baseTypeMapping = mappingFor(from)

        mappings.putIfAbsent(mapped.type, baseTypeMapping.derive(mapped))
    }

    inline fun <reified T : Any> register(
        crossinline writeJdbc: (stmt: PreparedStatement, index: Int, value: T) -> Unit,
        crossinline readJdbc: (rs: ResultSet, index: Int) -> T?
    ) = register(T::class, object : JdbcMappedType<T> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: T) =
            writeJdbc(stmt, index, value)

        override fun readJdbc(rs: ResultSet, index: Int): T? = readJdbc(rs, index)
    })

    inline fun <reified F : Any, reified T : Any> register(
        crossinline to: (F) -> T,
        crossinline from: (T) -> F
    ) = register<F, T>(F::class, object : TypeMapping<F, T> {
        override val type: KClass<T> = T::class

        override fun convert(value: F): T = to(value)
        override fun unconvert(value: T): F = from(value)
    })

    fun <F : Any, T : Any> register(mappedDataType: DataType<F, T>) {
        mappedDataType.mapping?.let { register(mappedDataType.dataType.type, it) }
    }

    @Suppress("unchecked_cast")
    fun <T : Any> mappingFor(type: KClass<T>): JdbcMappedType<T> =
        checkNotNull(mappings[type]) { "no JDBC mapping for $type" } as JdbcMappedType<T>
}