package io.koalaql.query.fluent

import io.koalaql.query.SqlPerformer
import io.koalaql.sql.SqlText

interface PerformableSql {
    fun generateSql(ds: SqlPerformer): SqlText?
}