package io.koalaql.query

import io.koalaql.query.built.BuiltDelete

interface Deleted: PerformableStatement {
    fun buildDelete(): BuiltDelete
}