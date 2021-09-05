package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltUpdate
import mfwgenerics.kotq.values.RowSequence

interface Updated: Performable<RowSequence> {
    fun buildUpdate(): BuiltUpdate
}