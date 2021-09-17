package io.koalaql.jdbc

import io.koalaql.sql.SqlText

class GeneratedSqlException(
    sql: SqlText,
    cause: Throwable
): Exception("$sql", cause) {
}