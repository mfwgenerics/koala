package io.koalaql.expr

import io.koalaql.query.Queryable
import io.koalaql.values.RowOfOne

interface ExprQueryable<T : Any>: Expr<T>, Queryable<RowOfOne<T>>