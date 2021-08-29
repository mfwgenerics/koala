package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltDelete

interface Deleted {
    fun buildDelete(): BuiltDelete
}