package io.koalaql.h2

import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import java.sql.Connection
import java.sql.DriverManager

fun H2DataSource(
    provider: JdbcProvider,
    declareStrategy: DeclareStrategy = DeclareStrategy.CreateIfNotExists
): JdbcDataSource = JdbcDataSource(
    JdbcSchemaDetection.NotSupported,
    H2Dialect(),
    provider,
    H2TypeMappings(),
    declareStrategy
)

fun H2Database(db: String): JdbcDataSource {
    val keepAlive = DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

    return H2DataSource(
        object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

            override fun close() {
                keepAlive.close()
            }
        }
    )
}