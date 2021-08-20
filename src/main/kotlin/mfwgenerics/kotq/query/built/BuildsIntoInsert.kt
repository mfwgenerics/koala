package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoInsert {
    fun buildInsert(): BuiltInsert =
        unfoldBuilder(BuiltInsert()) { buildIntoInsert(it) }

    fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert?
}