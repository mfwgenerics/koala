package io.koalaql.query

import io.koalaql.query.built.BuiltDml
import io.koalaql.sql.CompiledSql

interface SqlPerformer {
    fun generateSql(dml: BuiltDml): CompiledSql?
}