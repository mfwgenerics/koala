package io.koalaql

import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement
import io.koalaql.values.RowSequence

interface DataConnection: AutoCloseable {
    fun query(query: PerformableQuery): RowSequence
    fun statement(statement: PerformableStatement): Int

    fun commit()
    fun rollback()

    /* must guarantee changes are *not* committed */
    override fun close()
}