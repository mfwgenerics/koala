package io.koalaql.test.table

import io.koalaql.ddl.BOOLEAN
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR

object UserVenueTable: Table("UserVenue") {
    val user = column("user", VARCHAR(128).foreignKey(UserTable.id))
    val venue = column("venue", INTEGER.foreignKey(VenueTable.id))

    val visited = column("visited", BOOLEAN.default(false))
    val wantsToVisit = column("wantsToVisit", BOOLEAN.default(false))
    val favourited = column("favourited", BOOLEAN.default(false))

    init {
        primaryKey(user, venue)
    }
}