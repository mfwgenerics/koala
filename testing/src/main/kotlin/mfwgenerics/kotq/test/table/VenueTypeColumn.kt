package mfwgenerics.kotq.test.table

import mfwgenerics.kotq.data.TINYINT

val VENUE_TYPE = TINYINT.UNSIGNED.map({ VenueType.values()[it.toInt()] }, { it.ordinal.toUByte() })