package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.query.Inserted
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltInsert
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.query.fluent.Withed

class Insert(
    val of: Withed,
    val query: BuiltSubquery
): Inserted {
    override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
        out.query = query
        return of
    }
}