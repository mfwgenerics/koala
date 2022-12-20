package io.koalaql.test

import io.koalaql.ReconciledChanges
import io.koalaql.ReconciledDdl
import io.koalaql.event.ConnectionEventWriter
import io.koalaql.event.DataSourceChangeEvent
import io.koalaql.event.DataSourceEvent
import io.koalaql.sql.CompiledSql

class AppliedDdlListener(
    private val applied: ArrayList<CompiledSql> = arrayListOf()
): List<CompiledSql> by applied, DataSourceEvent {
    override fun changes(changes: ReconciledChanges, ddl: ReconciledDdl): DataSourceChangeEvent {
        return object : DataSourceChangeEvent {
            override fun applied(ddl: List<CompiledSql>) {
                applied.addAll(ddl)
            }
        }
    }

    override fun connect(): ConnectionEventWriter = ConnectionEventWriter.Discard
}