package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.built.BuiltQueryable
import io.koalaql.query.built.BuiltStatement
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence
import io.koalaql.values.SequenceToRowSequence

interface DataSource: BlockingPerformer {
    fun declareTables(tables: List<Table>)
    fun declareTables(vararg tables: Table) = declareTables(tables.asList())

    fun connect(isolation: Isolation, events: ConnectionEventWriter = ConnectionEventWriter.Discard): DataConnection

    override fun query(queryable: BuiltQueryable): RowSequence<RawResultRow> = transact {
        val rows = it.query(queryable)

        SequenceToRowSequence(
            rows.columns,
            rows.toList().asSequence()
        )
    }

    override fun statement(statement: BuiltStatement): Int = transact {
        it.statement(statement)
    }
}