package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.query.AliasedQueryable
import mfwgenerics.kotq.query.WithType
import mfwgenerics.kotq.query.built.*

interface Withable: Withed {
    private class WithQuery(
        val of: Withable,
        val type: WithType,
        val queries: List<AliasedQueryable>
    ): Withed, BuildsIntoSelect {
        override fun buildIntoInsert(out: BuiltInsert): BuildsIntoInsert? {
            out.withType = type
            out.withs = queries.map {
                BuiltWith(
                    it.alias,
                    it.queryable.buildQuery()
                )
            }

            return of
        }

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect {
            out.withType = type
            out.withs = queries.map {
                BuiltWith(
                    it.alias,
                    it.queryable.buildQuery()
                )
            }

            return of
        }
    }

    fun with(vararg queries: AliasedQueryable): Withed =
        WithQuery(this, WithType.NOT_RECURSIVE, queries.asList())
    fun withRecursive(vararg queries: AliasedQueryable): Withed =
        WithQuery(this, WithType.RECURSIVE, queries.asList())
}