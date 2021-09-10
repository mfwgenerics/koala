package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltDelete

interface Deleted: Performable<Unit> {
    fun buildDelete(): BuiltDelete
}