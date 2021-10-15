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
    ): Withed, BuildsIntoQueryBody {
        override fun BuiltInsert.buildIntoInsert(): BuildsIntoInsert? {
            withType = type
            withs = queries.map {
                BuiltWith(
                    it.cte,
                    it.queryable.buildQuery()
                )
            }

            return of
        }

        override fun BuiltQueryBody.buildIntoQueryBody(): BuildsIntoQueryBody {
            withType = type
            withs = queries.map {
                BuiltWith(
                    it.cte,
                    it.queryable.buildQuery()
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