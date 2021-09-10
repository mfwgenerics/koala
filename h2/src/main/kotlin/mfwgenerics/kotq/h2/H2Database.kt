package mfwgenerics.kotq.h2

import mfwgenerics.kotq.jdbc.JdbcDatabase
import mfwgenerics.kotq.jdbc.JdbcProvider
import java.sql.Connection
import java.sql.DriverManager

fun H2Database(db: String): JdbcDatabase {
    val keepAlive = DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

    return JdbcDatabase(
        H2Dialect(),
        /* MV_STORE=false or we get NPEs from h2 from a regression in 1.4.200
       we can't downgrade to 1.4.199 bc we need the date time features */
        object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

            override fun close() {
                keepAlive.close()
            }
        },
        H2TypeMappings()
    )
}