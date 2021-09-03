package mfwgenerics.kotq.test

import mfwgenerics.kotq.jdbc.ConnectionWithDialect

interface TestDatabase {
    val cxn: ConnectionWithDialect

    fun drop()
}