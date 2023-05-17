---
sidebar_position: 1
---

# MySQL

## Dependencies

```kotlin title="build.gradle.kts"
dependencies {
    implementation("io.koalaql:koala-mysql:0.0.9")
}
```

## Instantiating the database

Koala models your database as a `DataSource`. You should obtain one once at the start of your program.
The only required argument is the provider which supplies JDBC connections.
JDBC connections can be supplied directly from the driver or from a connection pool like [HikariCP](https://github.com/brettwooldridge/HikariCP)

```kotlin
val ds = MysqlDataSource(
    provider = { DriverManager.getConnection(jdbcUrl) }
)
```

:::info JDBC

Read more [here](https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html)
to learn more about JDBC for MySQL and see example values for `jdbcUrl`.

:::
