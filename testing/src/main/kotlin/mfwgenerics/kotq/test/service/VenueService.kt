package mfwgenerics.kotq.test.service

import mfwgenerics.kotq.dsl.*
import mfwgenerics.kotq.jdbc.JdbcDatabase
import mfwgenerics.kotq.jdbc.performWith
import mfwgenerics.kotq.test.models.NewVenue
import mfwgenerics.kotq.test.models.Venue
import mfwgenerics.kotq.test.table.UserTable
import mfwgenerics.kotq.test.table.UserVenueTable
import mfwgenerics.kotq.test.table.VenueTable

class VenueService(
    private val db: JdbcDatabase
) {
    init {
        db.createTables(UserTable, VenueTable, UserVenueTable)
    }

    fun createVenues(venues: List<NewVenue>): List<Int> = db.transact { cxn ->
        VenueTable
            .insert(values(venues) {
                set(VenueTable.name, it.name)
                set(VenueTable.description, it.description)
                set(VenueTable.closedPermanently, it.closed)
                set(VenueTable.type, it.type)
            })
            .returning(VenueTable.id)
            .performWith(cxn)
            .map { it.getOrNull(VenueTable.id)!! }
            .toList()
    }

    fun fetchVenues(ids: List<Int>? = null): List<Venue> {
        return db.transact { cxn ->
            val visits = name<Int>()
            val visitCte = cte()

            VenueTable
                .with(visitCte as_ UserVenueTable
                    .where(UserVenueTable.visited)
                    .groupBy(UserVenueTable.venue)
                    .select(UserVenueTable.venue, count() as_ visits)
                )
                .leftJoin(visitCte, UserVenueTable.venue eq VenueTable.id)
                .let {
                    if (ids != null) it.where(VenueTable.id inValues ids)
                    else it
                }
                .select(VenueTable, coalesce(visits, value(0)) as_ visits)
                .performWith(cxn)
                .map { row ->
                    Venue(
                        id = row[VenueTable.id],
                        created = row[VenueTable.created],
                        name = row[VenueTable.name],
                        description = row[VenueTable.description],
                        closed = row[VenueTable.closedPermanently],
                        rating = row[VenueTable.rating],
                        type = row[VenueTable.type],
                        visits = row[visits],
                    )
                }
                .toList()
        }
    }
}