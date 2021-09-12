import mfwgenerics.kotq.test.models.*
import mfwgenerics.kotq.test.service.VenueService
import mfwgenerics.kotq.test.table.UserTable
import mfwgenerics.kotq.test.table.UserVenueTable
import mfwgenerics.kotq.test.table.VenueTable
import mfwgenerics.kotq.test.table.VenueType
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class VenueSchemaTests: ProvideTestDatabase {
    fun withVenues(block: (VenueService) -> Unit) {
        withDb { db ->
            block(VenueService(db))
        }
    }

    @Test
    fun `create tables twice`() = withDb { db ->
        db.createTables(VenueTable, UserTable, UserVenueTable)
        db.createTables(VenueTable, UserTable, UserVenueTable)
    }

    private fun matchVenueFields(expected: NewVenue, actual: Venue) {
        assertEquals(expected.name, actual.name)
        assertEquals(expected.description, actual.description)
        assertEquals(expected.closed, actual.closed)
        assertEquals(expected.type, actual.type)
    }

    private fun matchReviewFields(expected: NewReview, venue: Venue, reviewIx: Int) {
        assertEquals(expected.venue, venue.id)

        val actual = venue.reviews[reviewIx]

        assertEquals(expected.user, actual.user)
        assertEquals(expected.content, actual.content)
    }

    @Test
    fun `create venues, review them and then delete reviews`() = withVenues { venues ->
        val exampleVenues = listOf(
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
        )

        val ids = venues.createVenues(exampleVenues)

        val afterCreation = venues.fetchVenues(ids.drop(1))

        repeat(2) {
            matchVenueFields(exampleVenues[it + 1], afterCreation[it])
        }

        val exampleReviews = listOf(
            NewReview(
                user = "user-0",
                venue = ids[1],
                content = "Great coffee!"
            ),
            NewReview(
                user = "user-0",
                venue = ids[2],
                content = "Wish they were still open"
            ),
            NewReview(
                user = "user-1",
                venue = ids[1],
                content = "Long wait times :("
            )
        )

        venues.mergeReviews(exampleReviews)

        val afterReviews = venues.fetchVenues()

        repeat(3) {
            matchVenueFields(exampleVenues[it], afterReviews[it])
        }

        matchReviewFields(exampleReviews[0], afterReviews[1], 0)
        matchReviewFields(exampleReviews[1], afterReviews[2], 0)
        matchReviewFields(exampleReviews[2], afterReviews[1], 1)

        assertEquals(afterReviews[0].reviews.size, 0)
        assertEquals(afterReviews[1].reviews.size, 2)
        assertEquals(afterReviews[2].reviews.size, 1)

        venues.deleteReviews(listOf(
            ReviewKey("user-0", ids[2]),
            ReviewKey("user-1", ids[1]),
            ReviewKey("user-0", ids[0]) /* <- doesn't exist */
        ))

        val afterDelete = venues.fetchVenues()

        repeat(3) {
            matchVenueFields(exampleVenues[it], afterDelete[it])
        }

        matchReviewFields(exampleReviews[0], afterDelete[1], 0)

        assertEquals(afterDelete[0].reviews.size, 0)
        assertEquals(afterDelete[1].reviews.size, 1)
        assertEquals(afterDelete[2].reviews.size, 0)
    }
}