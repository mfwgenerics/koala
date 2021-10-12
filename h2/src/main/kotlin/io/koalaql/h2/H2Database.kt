package io.koalaql.h2

import io.koalaql.DeclareStrategy
import io.koalaql.JdbcSchemaDetection
import io.koalaql.jdbc.JdbcDataSource
import io.koalaql.jdbc.JdbcProvider
import java.sql.Connection
import java.sql.DriverManager

fun H2DataSource(
    provider: JdbcProvider,
    declareStrategy: DeclareStrategy = DeclareStrategy.CreateIfNotExists,
    dialect: H2Dialect = H2Dialect()
): JdbcDataSource = JdbcDataSource(
    JdbcSchemaDetection.NotSupported,
    dialect,
    provider,
    H2TypeMappings(),
    declareStrategy
)

fun H2Database(db: String, mode: H2CompatibilityMode? = null): JdbcDataSource {
    val url = when (mode) {
        H2CompatibilityMode.MYSQL -> "jdbc:h2:mem:$db;MV_STORE=false;MODE=MYSQL"
        null -> "jdbc:h2:mem:$db;MV_STORE=false"
    }

    val keepAlive = DriverManager.getConnection(url)

    return H2DataSource(
        dialect = H2Dialect(mode),
        provider = object : JdbcProvider {
            override fun connect(): Connection =
                DriverManager.getConnection("jdbc:h2:mem:$db;MV_STORE=false")

            override fun close() {
                keepAlive.close()
            }
        }
    )
}