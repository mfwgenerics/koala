package io.koalaql.dialect

import io.koalaql.ddl.diff.SchemaDiff
import io.koalaql.query.built.BuiltStatement
import io.koalaql.sql.SqlText

interface SqlDialect {
    fun ddl(diff: SchemaDiff): List<SqlText>

    fun compile(statement: BuiltStatement): SqlText
}