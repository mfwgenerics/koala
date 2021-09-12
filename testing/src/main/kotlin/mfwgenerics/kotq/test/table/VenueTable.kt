package mfwgenerics.kotq.test.table

import mfwgenerics.kotq.data.*
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dsl.currentTimestamp
import mfwgenerics.kotq.dsl.keys

object VenueTable: Table("Venue") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())
    val created = column("create", INSTANT.default(currentTimestamp()))

    val name = column("name", VARCHAR(128))
    val description = column("description", TEXT)

    val closedPermanently = column("closed", BOOLEAN.default(false))

    val type = column("type", VENUE_TYPE.default(VenueType.RESTAURANT))

    val rating = column("rating", FLOAT.nullable())

    init {
        index(keys(closedPermanently, type, rating))
    }
}