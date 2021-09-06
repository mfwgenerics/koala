package mfwgenerics.kotq.jdbc

import java.sql.Connection

fun interface JdbcProvider {
    fun connect(): Connection
    fun close() { }
}