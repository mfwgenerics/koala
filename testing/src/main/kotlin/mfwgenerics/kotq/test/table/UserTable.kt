package mfwgenerics.kotq.test.table

import mfwgenerics.kotq.data.INSTANT
import mfwgenerics.kotq.data.TEXT
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dsl.currentTimestamp

object UserTable: Table("User") {
    val id = column("id", VARCHAR(128).primaryKey())
    val joined = column("join", INSTANT.default(currentTimestamp()))

    val username = column("username", VARCHAR(128))
    val bio = column("bio", TEXT)
}