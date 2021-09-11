package mfwgenerics.kotq.test.models

import java.time.Instant

data class VenueReview(
    val user: String,

    val created: Instant,
    val edited: Instant?,

    val content: String
)