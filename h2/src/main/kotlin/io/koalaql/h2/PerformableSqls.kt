package io.koalaql.h2

import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltDml
import io.koalaql.query.fluent.PerformableSql
import io.koalaql.sql.CompiledSql

fun PerformableSql.generateH2Sql() = generateSql(object : SqlPerformer {
    override fun generateSql(dml: BuiltDml): CompiledSql? =
        H2Dialect().compile(dml)
})