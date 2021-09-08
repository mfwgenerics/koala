package mfwgenerics.kotq.h2

import mfwgenerics.kotq.data.JdbcMappedType
import mfwgenerics.kotq.data.TypeMappings

import org.h2.api.TimestampWithTimeZone
import org.h2.util.DateTimeUtils
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant

fun H2TypeMappings(): TypeMappings {
    val result = TypeMappings()

    result.register(Instant::class, object : JdbcMappedType<Instant> {
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: Instant) {
            val vttz = DateTimeUtils.timestampTimeZoneFromMillis(value.toEpochMilli())

            TimestampWithTimeZone(
                vttz.dateValue,
                vttz.timeNanos,
                vttz.timeZoneOffsetMins
            )

            stmt.setObject(index, value)
        }

        override fun readJdbc(rs: ResultSet, index: Int): Instant? {
            val twtz = rs.getObject(index) as? TimestampWithTimeZone ?: return null

            return Instant.ofEpochMilli(
                DateTimeUtils.getMillis(twtz.ymd, twtz.nanosSinceMidnight, twtz.timeZoneOffsetMins)
            )
        }
    })

    return result
}