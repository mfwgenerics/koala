package io.koalaql.jdbc

import io.koalaql.sql.SqlText

class JdbcException(
    sql: SqlText,
    cause: Throwable
): Exception("$sql", cause)