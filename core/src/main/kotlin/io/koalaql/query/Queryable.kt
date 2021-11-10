package io.koalaql.query

import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText
import io.koalaql.values.RowSequence

interface Queryable<out T>: PerformableBlocking<RowSequence<T>>, QueryBuilder {
    override fun perform(ds: BlockingPerformer): RowSequence<T>

    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(BuiltQuery.from(this))

    fun subquery() = Subquery(BuiltQuery.from(this))
    fun subqueryAs(alias: Alias) = subquery().as_(alias)
}