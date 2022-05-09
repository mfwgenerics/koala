package io.koalaql.mysql

import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import io.koalaql.ddl.JsonData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun MysqlTypeMappings(): JdbcTypeMappings {
    val result = JdbcTypeMappings()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            stmt.setObject(index, value.atOffset(ZoneOffset.UTC).toLocalDateTime())
        }

        override fun readJdbc(rs: ResultSet, index: Int): Instant? {
            return (rs.getObject(index) as? LocalDateTime)?.toInstant(ZoneOffset.UTC)
        }
    })

    result.register(JsonData::class, object : JdbcMappedType<JsonData> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: JsonData) {
            stmt.setString(index, value.asString)
        }

        override fun readJdbc(rs: ResultSet, index: Int): JsonData? =
            rs.getString(index)?.let { JsonData(it) }
    })

    return result
}