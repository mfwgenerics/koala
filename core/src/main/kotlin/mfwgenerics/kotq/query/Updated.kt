package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltUpdate

interface Updated {
    fun buildUpdate(): BuiltUpdate
}