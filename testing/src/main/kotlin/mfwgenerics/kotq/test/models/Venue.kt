package mfwgenerics.kotq.test.models

import mfwgenerics.kotq.data.*
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.dsl.currentInstant
import mfwgenerics.kotq.dsl.keys

object Venue: Table("Restaurant") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())
    val created = column("create", INSTANT.default(currentInstant()))

    val name = column("name", VARCHAR(128))
    val description = column("description", TEXT)

    val closedPermanently = column("closed", BOOLEAN.default(false))

    val type = column("type", VENUE_TYPE.default(VenueType.RESTAURANT))

    val rating = column("rating", FLOAT.nullable())

    init {
        index(keys(closedPermanently, type, rating))
    }
}