package io.koalaql.event

import io.koalaql.sql.CompiledSql

interface DataSourceChangeEvent {
    fun applied(ddl: List<CompiledSql>)

    companion object {
        val DISCARD = object : DataSourceChangeEvent {
            override fun applied(ddl: List<CompiledSql>) { }
        }
    }
}