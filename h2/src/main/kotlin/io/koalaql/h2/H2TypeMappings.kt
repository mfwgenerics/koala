package io.koalaql.h2

import io.koalaql.data.JdbcMappedType
import io.koalaql.data.JdbcTypeMappings
import org.h2.api.TimestampWithTimeZone
import org.h2.util.TimeZoneProvider
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant

fun H2TypeMappings(): JdbcTypeMappings {
    val result = JdbcTypeMappings()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            stmt.setObject(index, value)
        }

        override fun readJdbc(rs: ResultSet, index: Int): Instant? {
            val twtz = rs.getObject(index) as? TimestampWithTimeZone ?: return null

            return Instant.ofEpochMilli(TimeZoneProvider
                .ofOffset(twtz.timeZoneOffsetSeconds)
                .getEpochSecondsFromLocal(twtz.ymd, twtz.nanosSinceMidnight)*1000 + twtz.nanosSinceMidnight/1000000 % 1000
            )
        }
    })

    return result
}