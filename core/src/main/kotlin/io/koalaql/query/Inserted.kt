package io.koalaql.query

import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder
import io.koalaql.query.fluent.PerformableBlocking

interface Inserted: PerformableBlocking<Int>, InsertBuilder {
    override fun performWith(ds: BlockingPerformer): Int =
        ds.statement(BuiltInsert.from(this))
}