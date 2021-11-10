package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltDelete
import io.koalaql.sql.SqlText

fun interface BuildsIntoDelete: PerformableBlocking<Int> {
    fun BuiltDelete.buildInto(): BuildsIntoDelete?

    override fun perform(ds: BlockingPerformer): Int = ds.statement(BuiltDelete.from(this))
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(BuiltDelete.from(this))
}