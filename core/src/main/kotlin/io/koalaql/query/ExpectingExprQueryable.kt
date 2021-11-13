package io.koalaql.query

import io.koalaql.expr.ExprQueryable
import io.koalaql.expr.Reference
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence
import io.koalaql.values.RowOfOne

class ExpectingExprQueryable<T : Any>(
    of: Queryable<*>,
    references: List<Reference<*>>,
    cast: (rows: RowSequence<RawResultRow>) -> RowSequence<RowOfOne<T>>
): ExprQueryable<T>, ExpectingQueryable<RowOfOne<T>>(
    of, references, cast
)