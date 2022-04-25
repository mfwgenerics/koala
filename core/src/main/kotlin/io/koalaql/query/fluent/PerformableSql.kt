package io.koalaql.query.fluent

import io.koalaql.query.SqlPerformer
import io.koalaql.sql.CompiledSql

interface PerformableSql {
    fun generateSql(ds: SqlPerformer): CompiledSql?
}