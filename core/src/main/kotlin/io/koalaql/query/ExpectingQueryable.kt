package io.koalaql.query

import io.koalaql.expr.Reference
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence

open class ExpectingQueryable<T>(
    val of: ExpectableSubqueryable<*>,
    val references: List<Reference<*>>,
    val cast: (rows: RowSequence<RawResultRow>) -> RowSequence<T>
): Subqueryable<T> {
    init {
        /* prematurely build query so we can check columns here and throw from .expecting(...) callsite */

        val query = with(of) { BuilderContext.buildQuery() }

        val columnSet = query.columns.toSet()

        check (references.size == columnSet.size && columnSet.containsAll(references)) {
            "call to .expecting failed.\nexpected references: $references\nfound columns: $columnSet"
        }
    }

    override fun perform(ds: BlockingPerformer): RowSequence<T> =
        cast(ds.query(with (this) { BuilderContext.buildQuery() }))

    override fun BuilderContext.buildQuery(): BuiltSubquery = with (of) {
        buildQuery(references)
    }
}