package io.koalaql.jdbc

import io.koalaql.sql.CompiledSql

class JdbcException(
    sql: CompiledSql,
    cause: Throwable
): Exception("$sql", cause)