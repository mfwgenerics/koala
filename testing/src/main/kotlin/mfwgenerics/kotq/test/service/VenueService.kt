package mfwgenerics.kotq.test.service

import mfwgenerics.kotq.dsl.values
import mfwgenerics.kotq.jdbc.JdbcDatabase
import mfwgenerics.kotq.test.models.NewVenue
import mfwgenerics.kotq.test.table.VenueTable

class VenueService(
    private val ds: JdbcDatabase
) {
    fun createVenues(venues: List<NewVenue>) = ds.transact {
        /*VenueTable
            .insert(venues.map {

            })*/
    }
}