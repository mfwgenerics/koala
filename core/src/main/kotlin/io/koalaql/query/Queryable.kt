package io.koalaql.query

import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface Queryable: PerformableBlocking<RowSequence<ResultRow>> {
    fun buildQuery(): BuiltSubquery
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(buildQuery())

    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> = ds.query(buildQuery())

    fun subquery() = Subquery(buildQuery())
    fun subqueryAs(alias: Alias) = subquery().as_(alias)
}