package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.query.built.BuiltStatement
import mfwgenerics.kotq.sql.SqlText

interface SqlDialect {
    fun ddl(diff: SchemaDiff): List<SqlText>

    fun compile(statement: BuiltStatement): SqlText
}