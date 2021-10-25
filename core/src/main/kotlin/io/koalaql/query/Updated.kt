package io.koalaql.query

import io.koalaql.query.built.BuiltUpdate
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText

interface Updated: PerformableBlocking<Int> {
    fun buildUpdate(): BuiltUpdate

    override fun performWith(ds: BlockingPerformer): Int = ds.statement(buildUpdate())
    override fun generateSql(ds: BlockingPerformer): SqlText? = ds.generateSql(buildUpdate())
}