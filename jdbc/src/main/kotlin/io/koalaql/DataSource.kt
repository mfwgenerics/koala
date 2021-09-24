package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.event.ConnectionEventWriter

interface DataSource {
    fun declareTables(tables: List<Table>)
    fun declareTables(vararg tables: Table) = declareTables(tables.asList())

    fun connect(isolation: Isolation, events: ConnectionEventWriter = ConnectionEventWriter.Discard): KotqConnection
}