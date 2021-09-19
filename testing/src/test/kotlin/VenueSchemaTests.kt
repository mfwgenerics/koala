import io.koalaql.test.models.*
import io.koalaql.test.service.OnConflictSupport
import io.koalaql.test.service.VenueService
import io.koalaql.test.table.UserTable
import io.koalaql.test.table.UserVenueTable
import io.koalaql.test.table.VenueTable
import io.koalaql.test.table.VenueType
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class VenueSchemaTests: ProvideTestDatabase {
    fun withVenues(block: (VenueService) -> Unit) {
        withDb { db ->
            block(VenueService(db, OnConflictSupport.NONE))
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

    @Test
    fun `visits`() = withVenues { venues ->
        val ids = venues.createVenues((0..49).map {
            NewVenue(
                name = "Chain Restaurant $it",
                description = "",
                closed = false,
                type = VenueType.RESTAURANT
            )
        })

        repeat(50) { ix ->
            venues.updateVisits(listOf(
                VenueVisitorUpdate(
                    venue = ids[ix],
                    user = "user-$ix",
                    state = true
                )
            ))
        }

        val actualSubset = venues.fetchVisits(users = listOf("user-0", "user-2", "user-3", "user-30"))

        val expectedSubset = setOf(
            UserVenueKey(1, "user-0"),
            UserVenueKey(3, "user-2"),
            UserVenueKey(4, "user-3"),
            UserVenueKey(31, "user-30")
        )

        assertSetEquals(expectedSubset, actualSubset)

        val bigMerge = (0..49)
            .flatMap { ix ->
                listOfNotNull(
                    if (ix < 49) {
                        VenueVisitorUpdate(
                            venue = ids[ix + 1],
                            user = "user-$ix",
                            state = true
                        )
                    } else null,
                    VenueVisitorUpdate(
                        venue = ids[ix],
                        user = "user-$ix",
                        state = (ix % 4 == 0)
                    )
                )
            }

        venues.updateVisits(bigMerge)

        val actualAll = venues.fetchVisits()

        val expectedAll = (0..49)
            .asSequence()
            .flatMap { ix ->
                listOfNotNull(
                    if (ix < 49) {
                        UserVenueKey(
                            venue = ids[ix + 1],
                            user = "user-$ix"
                        )
                    } else null,
                    if (ix % 4 == 0) {
                        UserVenueKey(
                            venue = ids[ix],
                            user = "user-$ix"
                        )
                    } else null
                )
            }
            .toSet()

        assertSetEquals(expectedAll, actualAll)
    }
}