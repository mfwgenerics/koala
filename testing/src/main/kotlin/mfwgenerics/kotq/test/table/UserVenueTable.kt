package mfwgenerics.kotq.test.table

import mfwgenerics.kotq.data.BOOLEAN
import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.Table

object UserVenueTable: Table("UserVenue") {
    val user = column("user", VARCHAR(128).reference(UserTable.id))
    val venue = column("venue", INTEGER.reference(VenueTable.id))

    val visited = column("visited", BOOLEAN.default(false))
    val wantsToVisit = column("wantsToVisit", BOOLEAN.default(false))
    val favourited = column("favourited", BOOLEAN.default(false))

    init {
        primaryKey(user, venue)
    }
}