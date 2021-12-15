package io.koalaql.query

import io.koalaql.expr.ExprQueryable
import io.koalaql.expr.Reference
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowOfOne
import io.koalaql.values.RowSequence

class ExpectingExprQueryable<T : Any>(
    of: ExpectableSubqueryable<*>,
    references: List<Reference<*>>,
    cast: (rows: RowSequence<RawResultRow>) -> RowSequence<RowOfOne<T>>
): ExprQueryable<T>, ExpectingQueryable<RowOfOne<T>>(
    of, references, cast
)