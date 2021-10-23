package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface BlockingPerformer {
    fun query(query: BuiltQuery): RowSequence<ResultRow>
    fun statement(statement: BuiltStatement): Int
}