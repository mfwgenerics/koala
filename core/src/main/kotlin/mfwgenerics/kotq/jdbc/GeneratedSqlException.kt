package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.sql.SqlText

class GeneratedSqlException(
    sql: SqlText,
    cause: Throwable
): Exception("$sql", cause) {
}