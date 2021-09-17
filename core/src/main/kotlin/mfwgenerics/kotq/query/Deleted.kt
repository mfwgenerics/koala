package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltDelete

interface Deleted: PerformableStatement {
    fun buildDelete(): BuiltDelete
}