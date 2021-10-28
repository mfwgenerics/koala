package io.koalaql.query

import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText
import io.koalaql.values.RowSequence

interface Queryable<T>: PerformableBlocking<RowSequence<T>> {
    fun buildQuery(): BuiltSubquery
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(buildQuery())

    override fun performWith(ds: BlockingPerformer): RowSequence<T>

    fun subquery() = Subquery(buildQuery())
    fun subqueryAs(alias: Alias) = subquery().as_(alias)
}