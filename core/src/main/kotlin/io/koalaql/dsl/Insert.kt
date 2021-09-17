package io.koalaql.dsl

import io.koalaql.query.Inserted
import io.koalaql.query.built.BuildsIntoInsert
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.fluent.Withed

class Insert(
    val of: Withed,
    val query: BuiltSubquery
): Inserted {
    override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
        out.query = query
        return of
    }
}