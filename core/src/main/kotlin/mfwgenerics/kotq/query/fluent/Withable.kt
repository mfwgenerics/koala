package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.CtedQueryable
import mfwgenerics.kotq.query.WithOperand
import mfwgenerics.kotq.query.WithType
import mfwgenerics.kotq.query.built.*

interface Withable: Withed {
    private class WithQuery(
        val of: Withable,
        val type: WithType,
        val queries: List<CtedQueryable>
    ): Withed, BuildsIntoQueryBody {
        override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
            out.withType = type
            out.withs = queries.map {
                BuiltWith(
                    it.cte,
                    it.queryable.buildQuery()
                )
            }

            return of
        }

        override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody {
            out.withType = type
            out.withs = queries.map {
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