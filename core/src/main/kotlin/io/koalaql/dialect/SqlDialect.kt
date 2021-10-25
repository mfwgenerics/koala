package io.koalaql.dialect

import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.query.built.BuiltDml
import io.koalaql.sql.SqlText

interface SqlDialect {
    fun ddl(change: SchemaChange): List<SqlText>

    /*
    implementations can return null to indicate a no-op.
    this should be done in situations where:

    1. `dml` is a case that can not be strictly represented in SQL. e.g. empty IN clause, empty values
    2. if it *were* representable in SQL, the SQL would clearly be a no-op. e.g. top level `DELETE ... WHERE x IN [empty list]`
    */
    fun compile(dml: BuiltDml): SqlText?
}