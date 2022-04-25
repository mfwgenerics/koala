package io.koalaql.query

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.sql.CompiledSql
import io.koalaql.values.RowSequence

interface Subqueryable<out T>: PerformableBlocking<RowSequence<T>> {
    fun BuilderContext.buildQuery(): BuiltSubquery

    fun subquery() = Subquery(with (this) { BuilderContext.buildQuery() })
    fun subqueryAs(alias: Alias) = subquery().as_(alias)

    override fun generateSql(ds: SqlPerformer): CompiledSql? =
        ds.generateSql(with (this) { BuilderContext.buildQuery() })
}