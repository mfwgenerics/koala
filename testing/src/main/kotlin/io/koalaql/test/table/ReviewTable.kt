package io.koalaql.test.table

import io.koalaql.ddl.TIMESTAMP
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.TEXT
import io.koalaql.ddl.VARCHAR
import io.koalaql.ddl.Table
import io.koalaql.dsl.currentTimestamp

object ReviewTable: Table("Review") {
    val user = column("user", VARCHAR(128).reference(UserTable.id))
    val venue = column("venue", INTEGER.reference(VenueTable.id))

    val created = column("create", TIMESTAMP.default(currentTimestamp()))
    val edited = column("edit", TIMESTAMP.nullable().default(null))

    val contents = column("content", TEXT)

    init {
        primaryKey(user, venue)

        index("historical venue reviews", venue, created)
    }
}