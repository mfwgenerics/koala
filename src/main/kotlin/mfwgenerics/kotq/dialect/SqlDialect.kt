package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.dsl.Queryable
import mfwgenerics.kotq.query.built.BuiltStatement
import mfwgenerics.kotq.sql.SqlText

interface SqlDialect {
    fun compile(statement: BuiltStatement): SqlText

    fun compileQueryable(queryable: Queryable): SqlText =
        compile(queryable.buildQuery())
}