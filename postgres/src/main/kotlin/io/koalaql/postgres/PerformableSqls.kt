package io.koalaql.postgres

import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltDml
import io.koalaql.query.fluent.PerformableSql
import io.koalaql.sql.CompiledSql

fun PerformableSql.generatePostgresSql() = generateSql(object : SqlPerformer {
    override fun generateSql(dml: BuiltDml): CompiledSql? =
        PostgresDialect().compile(dml)
})
