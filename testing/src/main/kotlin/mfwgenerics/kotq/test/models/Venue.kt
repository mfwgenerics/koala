package mfwgenerics.kotq.test.models

import mfwgenerics.kotq.test.table.VenueType
import java.time.Instant

data class Venue(
    val id: Int,
    val created: Instant,

    val name: String,
    val description: String,

    val closed: Boolean,

    val type: VenueType,
    val rating: Float?,

    val visits: Int,

    val reviews: List<VenueReview>
)
