package io.koalaql.postgres

import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

fun PostgresTypeMappings(): JdbcTypeMappings {
    val result = JdbcTypeMappings()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            stmt.setTimestamp(index, Timestamp.from(value))
        }

        override fun readJdbc(rs: ResultSet, index: Int): Instant? {
            return rs.getTimestamp(index).toInstant()
        }
    })

    return result
}