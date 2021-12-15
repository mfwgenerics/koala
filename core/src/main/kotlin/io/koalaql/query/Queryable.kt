package io.koalaql.query

import io.koalaql.expr.Reference
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.sql.SqlText
import io.koalaql.unfoldBuilder
import io.koalaql.values.RowSequence

interface Queryable<out T>: ExpectableSubqueryable<T>, QueryBuilder {
    override fun BuilderContext.buildQuery(expectedColumns: List<Reference<*>>?): BuiltQuery {
        return unfoldBuilder(this@Queryable as QueryBuilder, BuiltQuery()) { it.buildInto() }
            .apply { finishBuild(expectedColumns) }
    }

    override fun BuilderContext.buildQuery(): BuiltQuery = buildQuery(null)
}