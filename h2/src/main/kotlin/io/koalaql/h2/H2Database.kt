package io.koalaql.h2

import io.koalaql.jdbc.JdbcDatabase
import io.koalaql.jdbc.JdbcProvider
import java.sql.Connection
import java.sql.DriverManager

fun H2Database(db: String): JdbcDatabase {
    val keepAlive = DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

    return JdbcDatabase(
        H2Dialect(),
        /* workaround MV_STORE=false or we get NPEs from h2 from a regression in 1.4.200
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