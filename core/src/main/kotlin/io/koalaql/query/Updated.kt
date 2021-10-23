package io.koalaql.query

import io.koalaql.query.built.BuiltUpdate
import io.koalaql.query.fluent.PerformableBlocking

interface Updated: PerformableBlocking<Int> {
    fun buildUpdate(): BuiltUpdate

    override fun performWith(ds: BlockingPerformer): Int = ds.statement(buildUpdate())
}