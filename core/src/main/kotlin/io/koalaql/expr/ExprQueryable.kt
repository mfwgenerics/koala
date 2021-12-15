package io.koalaql.expr

import io.koalaql.query.Subqueryable
import io.koalaql.values.RowOfOne

interface ExprQueryable<T : Any>: Expr<T>, Subqueryable<RowOfOne<T>>