package io.koalaql.query

import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.query.built.FullQueryBuilder
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.SqlText
import io.koalaql.values.RowSequence

interface Queryable<out T>: PerformableBlocking<RowSequence<T>>, FullQueryBuilder {
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(BuiltFullQuery.from(this))

    override fun performWith(ds: BlockingPerformer): RowSequence<T>

    fun subquery() = Subquery(BuiltFullQuery.from(this))
    fun subqueryAs(alias: Alias) = subquery().as_(alias)
}