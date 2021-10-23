package io.koalaql.query

import io.koalaql.query.built.BuiltDelete
import io.koalaql.query.fluent.PerformableBlocking

interface Deleted: PerformableBlocking<Int> {
    fun buildDelete(): BuiltDelete

    override fun performWith(ds: BlockingPerformer): Int =
        ds.statement(buildDelete())
}