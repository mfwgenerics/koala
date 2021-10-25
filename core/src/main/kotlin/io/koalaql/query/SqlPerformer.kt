package io.koalaql.query

import io.koalaql.query.built.BuiltDml
import io.koalaql.sql.SqlText

interface SqlPerformer {
    fun generateSql(dml: BuiltDml): SqlText?
}