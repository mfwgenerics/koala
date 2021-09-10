import mfwgenerics.kotq.jdbc.JdbcConnection
import mfwgenerics.kotq.test.models.NewVenue
import mfwgenerics.kotq.test.service.VenueService
import mfwgenerics.kotq.test.table.UserTable
import mfwgenerics.kotq.test.table.UserVenueTable
import mfwgenerics.kotq.test.table.VenueTable
import mfwgenerics.kotq.test.table.VenueType
import kotlin.test.Test

abstract class VenueSchemaTests: ProvideTestDatabase {
    fun withVenues(block: (VenueService) -> Unit) {
        withDb { db ->
            block(VenueService(db))
        }
    }

    @Test
    fun `create schema`() = withDb { db ->
        db.createTables(VenueTable, UserTable, UserVenueTable)
        db.createTables(VenueTable, UserTable, UserVenueTable)
    }

    @Test
    fun `create and fetch venues`() = withVenues { venues ->
        val ids = venues.createVenues(listOf(
            NewVenue(
                name = "Tom's Tavern",
                description = "",
                closed = false,
                type = VenueType.BAR
            ),
            NewVenue(
                name = "Carie's Cafe",
                description = "Espresso in a relaxed setting",
                closed = false,
                type = VenueType.CAFE
            ),
            NewVenue(
                name = "Bill's Burgers",
                description = "Shut down by health inspector",
                closed = true,
                type = VenueType.RESTAURANT
            )
        ))

        println(venues.fetchVenues(ids.drop(1)))
    }
}