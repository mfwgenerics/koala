package io.koalaql.event

import io.koalaql.ReconciledChanges
import io.koalaql.ReconciledDdl

interface DataSourceEvent {
    fun changes(changes: ReconciledChanges, ddl: ReconciledDdl): DataSourceChangeEvent

    fun connect(): ConnectionEventWriter

    companion object {
        val DISCARD = object : DataSourceEvent {
            override fun changes(changes: ReconciledChanges, ddl: ReconciledDdl): DataSourceChangeEvent =
                DataSourceChangeEvent.DISCARD

            override fun connect(): ConnectionEventWriter = ConnectionEventWriter.Discard
        }
    }
}