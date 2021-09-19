package io.koalaql.mysql

import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
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

    return result
}