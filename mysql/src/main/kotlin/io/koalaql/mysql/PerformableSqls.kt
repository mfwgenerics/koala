package io.koalaql.mysql

import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltDml
import io.koalaql.query.fluent.PerformableSql
import io.koalaql.sql.SqlText

fun PerformableSql.generateMysqlSql() = generateSql(object : SqlPerformer {
    override fun generateSql(dml: BuiltDml): SqlText? =
        MysqlDialect().compile(dml)
})