package io.koalaql.test.models

import io.koalaql.test.table.VenueType

data class NewVenue(
    val name: String,
    val description: String,

    val closed: Boolean,

    val type: VenueType
)
