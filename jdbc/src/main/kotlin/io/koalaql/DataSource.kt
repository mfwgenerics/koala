package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.RawResultRow
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence
import io.koalaql.values.SequenceToRowSequence

interface DataSource: BlockingPerformer {
    fun declareTables(tables: List<Table>)
    fun declareTables(vararg tables: Table) = declareTables(tables.asList())

    fun connect(isolation: Isolation, events: ConnectionEventWriter = ConnectionEventWriter.Discard): DataConnection

    override fun query(query: BuiltQuery): RowSequence<RawResultRow> = transact {
        val rows = it.query(query)

        SequenceToRowSequence(
            rows.columns,
            rows.toList().asSequence()
        )
    }

    override fun statement(statement: BuiltStatement): Int = transact {
        it.statement(statement)
    }
}