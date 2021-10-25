package io.koalaql.test.table

import io.koalaql.ddl.*
import io.koalaql.dsl.currentTimestamp
import io.koalaql.dsl.keys

object VenueTable: Table("Venue") {
    val created = column("create", TIMESTAMP.default(currentTimestamp()))

    val name = column("name", VARCHAR(128))
    val description = column("description", TEXT)

    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val closedPermanently = column("closed", BOOLEAN.default(false))

    val type = column("type", VENUE_TYPE.default(VenueType.RESTAURANT))

    val rating = column("rating", FLOAT.nullable())

    init {
        index(keys(closedPermanently, type, rating))
    }
}