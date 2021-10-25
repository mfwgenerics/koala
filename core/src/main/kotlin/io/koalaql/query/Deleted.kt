package io.koalaql.query

import io.koalaql.query.built.BuiltDelete
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText

interface Deleted: PerformableBlocking<Int> {
    fun buildDelete(): BuiltDelete

    override fun performWith(ds: BlockingPerformer): Int = ds.statement(buildDelete())
    override fun generateSql(ds: BlockingPerformer): SqlText? = ds.generateSql(buildDelete())
}