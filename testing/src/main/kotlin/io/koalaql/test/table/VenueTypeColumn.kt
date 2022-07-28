package io.koalaql.test.table

import io.koalaql.ddl.TINYINT

val VENUE_TYPE = TINYINT.UNSIGNED.mapToEnum<VenueType> { it.ordinal.toUByte() }