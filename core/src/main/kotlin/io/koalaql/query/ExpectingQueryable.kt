package io.koalaql.query

import io.koalaql.expr.Reference
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

open class ExpectingQueryable<T>(
    val of: Queryable<*>,
    val references: List<Reference<*>>,
    val cast: (rows: RowSequence<RawResultRow>) -> RowSequence<T>
): Queryable<T> {
    init {
        /* prematurely build query so we can check columns here and throw from .expecting(...) callsite */

        val query = BuiltQuery.from(of)

        val columnSet = query.columns.toSet()

        check (references.size == columnSet.size && columnSet.containsAll(references)) {
            "call to .expecting failed.\nexpected references: $references\nfound columns: $columnSet"
        }
    }

    override fun perform(ds: BlockingPerformer): RowSequence<T> =
        cast(ds.query(BuiltQuery.from(this)))

    override fun BuiltQuery.buildInto(): QueryBuilder {
        expectedColumnOrder = references
        return of
    }
}