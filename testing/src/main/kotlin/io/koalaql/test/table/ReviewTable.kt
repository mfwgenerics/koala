package io.koalaql.test.table

import io.koalaql.ddl.*
import io.koalaql.dsl.currentTimestamp

object ReviewTable: Table("Review") {
    val user = column("user", VARCHAR(128).foreignKey(UserTable.id))
    val venue = column("venue", INTEGER.foreignKey(VenueTable.id))

    val created = column("create", TIMESTAMP.default(currentTimestamp()))
    val edited = column("edit", TIMESTAMP.nullable().default(null))

    val contents = column("content", TEXT)

    init {
        primaryKey(user, venue)

        index("historical venue reviews", venue, created)
    }
}