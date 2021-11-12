package io.koalaql.expr

import io.koalaql.query.Queryable
import io.koalaql.values.RowWithOneColumn

interface ExprQueryable<T : Any>: Expr<T>, Queryable<RowWithOneColumn<T>>