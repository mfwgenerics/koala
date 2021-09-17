package io.koalaql.query

import io.koalaql.query.built.BuiltUpdate
import io.koalaql.values.RowSequence

interface Updated: PerformableStatement {
    fun buildUpdate(): BuiltUpdate
}