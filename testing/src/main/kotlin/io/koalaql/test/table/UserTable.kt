package io.koalaql.test.table

import io.koalaql.ddl.TIMESTAMP
import io.koalaql.ddl.TEXT
import io.koalaql.ddl.VARCHAR
import io.koalaql.ddl.Table
import io.koalaql.dsl.currentTimestamp

object UserTable: Table("User") {
    val id = column("id", VARCHAR(128).primaryKey())
    val joined = column("join", TIMESTAMP.default(currentTimestamp()))

    val username = column("username", VARCHAR(128))
    val bio = column("bio", TEXT)
}