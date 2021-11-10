package io.koalaql.query

import io.koalaql.query.built.BuiltQueryable
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

interface BlockingPerformer: SqlPerformer {
    fun query(queryable: BuiltQueryable): RowSequence<RawResultRow>
    fun statement(statement: BuiltStatement): Int
}