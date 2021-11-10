package io.koalaql.query.fluent

import io.koalaql.query.CtedQueryable
import io.koalaql.query.WithOperand
import io.koalaql.query.WithType
import io.koalaql.query.built.*

interface Withable: Withed {
    private class WithQuery(
        val of: Withable,
        val type: WithType,
        val queries: List<CtedQueryable>
    ): Withed, QueryBodyBuilder {
        override fun BuiltInsert.buildIntoInsert(): InsertBuilder? {
            withType = type
            withs = queries.map {
                BuiltWith(
                    it.cte,
                    BuiltFullQuery.from(it.queryable)
                )
            }

            return of
        }

        override fun BuiltQueryBody.buildIntoQueryBody(): QueryBodyBuilder {
            withType = type
            withs = queries.map {
                BuiltWith(
                    it.cte,
                    BuiltFullQuery.from(it.queryable)
                )
            }

            return of
        }
    }

    fun with(vararg queries: WithOperand): Withed =
        WithQuery(this, WithType.NOT_RECURSIVE, queries.map { it.buildCtedQueryable() })
    fun withRecursive(vararg queries: WithOperand): Withed =
        WithQuery(this, WithType.RECURSIVE, queries.map { it.buildCtedQueryable() })
}