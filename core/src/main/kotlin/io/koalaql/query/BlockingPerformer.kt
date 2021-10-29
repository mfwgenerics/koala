package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

interface BlockingPerformer: SqlPerformer {
    fun query(query: BuiltQuery): RowSequence<RawResultRow>
    fun statement(statement: BuiltStatement): Int
}