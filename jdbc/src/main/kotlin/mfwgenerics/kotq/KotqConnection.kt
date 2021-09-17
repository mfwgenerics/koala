package mfwgenerics.kotq

import mfwgenerics.kotq.query.PerformableQuery
import mfwgenerics.kotq.query.PerformableStatement
import mfwgenerics.kotq.values.RowSequence

interface KotqConnection {
    fun perform(query: PerformableQuery): RowSequence
    fun perform(statement: PerformableStatement): Int

    fun commit()
    fun rollback()

    /* should guarantee changes are not committed */
    fun close()
}