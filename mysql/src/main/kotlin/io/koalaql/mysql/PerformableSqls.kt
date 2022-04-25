package io.koalaql.mysql

import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltDml
import io.koalaql.query.fluent.PerformableSql
import io.koalaql.sql.CompiledSql

fun PerformableSql.generateMysqlSql() = generateSql(object : SqlPerformer {
    override fun generateSql(dml: BuiltDml): CompiledSql? =
        MysqlDialect().compile(dml)
})