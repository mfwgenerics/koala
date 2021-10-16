package io.koalaql.test.table

import io.koalaql.ddl.TINYINT

val VENUE_TYPE = TINYINT.UNSIGNED.map({ VenueType.values()[it.toInt()] }, { it.ordinal.toUByte() })