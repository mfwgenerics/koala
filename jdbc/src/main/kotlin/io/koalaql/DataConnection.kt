package io.koalaql

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface DataConnection: BlockingPerformer, AutoCloseable {
    override fun query(query: BuiltQuery): RowSequence<ResultRow>
    override fun statement(statement: BuiltStatement): Int

    fun commit()
    fun rollback()

    /* must guarantee changes are *not* committed */
    override fun close()
}