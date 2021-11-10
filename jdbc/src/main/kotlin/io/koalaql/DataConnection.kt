package io.koalaql

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.built.BuiltQueryable
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

interface DataConnection: BlockingPerformer, AutoCloseable {
    override fun query(queryable: BuiltQueryable): RowSequence<RawResultRow>
    override fun statement(statement: BuiltStatement): Int

    fun commit()
    fun rollback()

    /* must guarantee changes are *not* committed */
    override fun close()
}