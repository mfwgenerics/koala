package io.koalaql.h2

import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder

fun H2TypeMappings(): JdbcTypeMappings {
    val result = JdbcTypeMappings()

    val offsetDateTime = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(ISO_LOCAL_TIME)
        .appendOffset("+HH:mm", "")
        .toFormatter()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            stmt.setObject(index, value)
        }

        override fun readJdbc(rs: ResultSet, index: Int): Instant? {
            return rs.getString(index)?.let { offsetDateTime.parse(it, ZonedDateTime::from).toInstant() }
        }
    })

    return result
}