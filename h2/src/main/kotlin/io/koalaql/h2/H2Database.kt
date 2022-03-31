package io.koalaql.h2

import io.koalaql.CreateIfNotExists
import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.event.DataSourceEvent
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import java.sql.Connection
import java.sql.DriverManager

fun H2DataSource(
    provider: JdbcProvider,
    declareStrategy: DeclareStrategy = CreateIfNotExists,
    dialect: H2Dialect = H2Dialect(),
    events: DataSourceEvent = DataSourceEvent.DISCARD
): JdbcDataSource = JdbcDataSource(
    JdbcSchemaDetection.NotSupported,
    dialect,
    provider,
    H2TypeMappings(),
    declareStrategy,
    events
)

fun H2Database(
    db: String,
    mode: H2CompatibilityMode? = null,
    declareBy: DeclareStrategy? = CreateIfNotExists
): JdbcDataSource {
    val url = when (mode) {
        H2CompatibilityMode.MYSQL -> "jdbc:h2:mem:$db;MODE=MYSQL"
        null -> "jdbc:h2:mem:$db"
    }

    val keepAlive = DriverManager.getConnection(url)

    return H2DataSource(
        dialect = H2Dialect(mode),
        declareStrategy = declareBy?:CreateIfNotExists,
        provider = object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection(url)

            override fun close() {
                keepAlive.close()
            }
        }
    )
}