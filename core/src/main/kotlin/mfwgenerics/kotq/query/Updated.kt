package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltUpdate
import mfwgenerics.kotq.values.RowSequence

interface Updated: PerformableStatement {
    fun buildUpdate(): BuiltUpdate
}