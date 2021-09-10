package mfwgenerics.kotq.h2

import mfwgenerics.kotq.data.JdbcMappedType
import mfwgenerics.kotq.data.JdbcTypeMappings

import org.h2.api.TimestampWithTimeZone
import org.h2.util.DateTimeUtils
import org.h2.util.TimeZoneProvider
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.util.*

fun H2TypeMappings(): JdbcTypeMappings {
    val result = JdbcTypeMappings()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            val vttz = DateTimeUtils.timestampTimeZoneFromMillis(value.toEpochMilli())

            TimestampWithTimeZone(
                vttz.dateValue,
                vttz.timeNanos,
                vttz.timeZoneOffsetSeconds
            )

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