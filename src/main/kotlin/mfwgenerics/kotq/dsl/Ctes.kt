package mfwgenerics.kotq.dsl

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.query.CtedQueryable
import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.query.Subqueryable

fun cte(identifier: String? = null): Cte =
    Cte(IdentifierName(identifier))

infix fun Cte.`as`(queryable: Subqueryable): CtedQueryable =
    CtedQueryable(this, queryable)