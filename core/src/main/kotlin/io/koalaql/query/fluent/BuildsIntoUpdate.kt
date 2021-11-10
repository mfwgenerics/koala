package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltUpdate
import io.koalaql.sql.SqlText

fun interface BuildsIntoUpdate: PerformableBlocking<Int> {
    fun BuiltUpdate.buildInto(): BuildsIntoUpdate?

    override fun perform(ds: BlockingPerformer): Int = ds.statement(BuiltUpdate.from(this))
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(BuiltUpdate.from(this))
}