package mfwgenerics.kotq.test.models

import mfwgenerics.kotq.test.table.VenueType

data class NewVenue(
    val name: String,
    val description: String,

    val closed: Boolean,

    val type: VenueType
)
