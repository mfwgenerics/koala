package mfwgenerics.kotq.test.table

import mfwgenerics.kotq.data.INSTANT
import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.TEXT
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dsl.currentTimestamp

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