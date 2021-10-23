package io.koalaql.dialect

import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.query.built.BuiltDml
import io.koalaql.sql.SqlText

interface SqlDialect {
    fun ddl(change: SchemaChange): List<SqlText>

    fun compile(dml: BuiltDml): SqlText
}