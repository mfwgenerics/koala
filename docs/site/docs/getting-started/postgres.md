---
sidebar_position: 2
---

# PostgreSQL

## Dependencies

```kotlin title="build.gradle.kts"
dependencies {
    implementation("io.koalaql:koala-postgres:0.0.11")
}
```

## Instantiating the database

Koala models your database as a `DataSource`. You should obtain one once at the start of your program.
The only required argument is the provider which supplies JDBC connections.
JDBC connections can be supplied directly from the driver or from a connection pool like [HikariCP](https://github.com/brettwooldridge/HikariCP)

```kotlin
val ds = PostgresDataSource(
    provider = { DriverManager.getConnection(jdbcUrl) }
)
```

:::info JDBC

Read more [here](https://jdbc.postgresql.org/documentation/use/)
to learn more about JDBC for PostgreSQL and see example values for `jdbcUrl`.

:::
