package io.koalaql.test.table

import io.koalaql.data.INSTANT
import io.koalaql.data.INTEGER
import io.koalaql.data.TEXT
import io.koalaql.data.VARCHAR
import io.koalaql.ddl.Table
import io.koalaql.dsl.currentTimestamp

object ReviewTable: Table("Review") {
    val user = column("user", VARCHAR(128).reference(UserTable.id))
    val venue = column("venue", INTEGER.reference(VenueTable.id))

    val created = column("create", INSTANT.default(currentTimestamp()))
    val edited = column("edit", INSTANT.nullable().default(null))

    val contents = column("content", TEXT)

    init {
        primaryKey(user, venue)

        index("historical venue reviews", venue, created)
    }
}