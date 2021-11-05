package io.koalaql.event

import io.koalaql.sql.SqlText

interface DataSourceChangeEvent {
    fun applied(ddl: List<SqlText>)

    companion object {
        val DISCARD = object : DataSourceChangeEvent {
            override fun applied(ddl: List<SqlText>) { }
        }
    }
}