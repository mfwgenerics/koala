package io.koalaql.query

import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText

interface Inserted: PerformableBlocking<Int>, InsertBuilder {
    override fun performWith(ds: BlockingPerformer): Int = ds.statement(BuiltInsert.from(this))
    override fun generateSql(ds: BlockingPerformer): SqlText? = ds.generateSql(BuiltInsert.from(this))
}