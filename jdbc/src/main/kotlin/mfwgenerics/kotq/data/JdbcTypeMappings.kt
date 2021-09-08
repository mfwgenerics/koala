package mfwgenerics.kotq.data

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JdbcTypeMappings {
    private val mappings = ConcurrentHashMap<KClass<*>, JdbcMappedType<*>>()

    fun <T : Any> register(type: KClass<T>, mapping: JdbcMappedType<T>) {
        mappings[type] = mapping
    }

    init {
        register(Int::class, object : JdbcMappedType<Int> {
            override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Int) {
                stmt.setInt(index, value)
            }

            override fun readJdbc(rs: ResultSet, index: Int): Int? {
                return rs.getInt(index).takeUnless { rs.wasNull() }
            }
        })

        register(String::class, object : JdbcMappedType<String> {
            override fun writeJdbc(stmt: PreparedStatement, index: Int, value: String) {
                stmt.setString(index, value)
            }

            override fun readJdbc(rs: ResultSet, index: Int): String? {
                return rs.getString(index)
            }
        })
    }

    fun <F : Any, T : Any> register(from: KClass<F>, mapped: TypeMapping<F, T>) {
        val baseTypeMapping = mappingFor(from)

        mappings.putIfAbsent(mapped.type, baseTypeMapping.derive(mapped))
    }

    fun <F : Any, T : Any> register(mappedDataType: DataType<F, T>) {
        mappedDataType.mapping?.let { register(mappedDataType.dataType.type, it) }
    }

    @Suppress("unchecked_cast")
    fun <T : Any> mappingFor(type: KClass<T>): JdbcMappedType<T> =
        checkNotNull(mappings[type]) { "no JDBC mapping for $type" } as JdbcMappedType<T>
}