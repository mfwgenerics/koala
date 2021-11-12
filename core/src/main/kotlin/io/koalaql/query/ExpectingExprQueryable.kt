package io.koalaql.query

import io.koalaql.expr.ExprQueryable
import io.koalaql.expr.Reference
import io.koalaql.query.built.BuiltQuery
import io.koalaql.query.built.QueryBuilder
import io.koalaql.values.RawResultRow
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithOneColumn

class ExpectingExprQueryable<T : Any>(
    of: Queryable<*>,
    references: List<Reference<*>>,
    cast: (rows: RowSequence<RawResultRow>) -> RowSequence<RowWithOneColumn<T>>
): ExprQueryable<T>, ExpectingQueryable<RowWithOneColumn<T>>(
    of, references, cast
)