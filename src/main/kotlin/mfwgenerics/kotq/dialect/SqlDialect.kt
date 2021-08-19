package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.Queryable
import mfwgenerics.kotq.query.Statement
import mfwgenerics.kotq.sql.SqlText

interface SqlDialect {
    fun compile(statement: Statement): SqlText

    fun compileQueryable(queryable: Queryable): SqlText =
        compile(queryable.buildSelect())
}