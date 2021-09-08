package mfwgenerics.kotq.data

import java.sql.PreparedStatement
import java.sql.ResultSet

interface JdbcMappedType<T : Any> {
    fun writeJdbc(stmt: PreparedStatement, index: Int, value: T)
    fun readJdbc(rs: ResultSet, index: Int): T?

    fun <R : Any> derive(mapping: TypeMapping<T, R>): JdbcMappedType<R> = object : JdbcMappedType<R> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: R) {
            this@JdbcMappedType.writeJdbc(stmt, index, mapping.unconvert(value))
        }

        override fun readJdbc(rs: ResultSet, index: Int): R? =
            this@JdbcMappedType.readJdbc(rs, index)?.let(mapping::convert)
    }
}