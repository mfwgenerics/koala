package io.koalaql.query

import io.koalaql.query.built.BuiltSubquery
import io.koalaql.sql.SqlText

interface Subqueryable: Queryable {
    override fun buildQuery(): BuiltSubquery
    override fun generateSql(ds: SqlPerformer): SqlText? = ds.generateSql(buildQuery())

    fun subquery() = Subquery(buildQuery())
    fun subquery(alias: Alias) = subquery().as_(alias)
}