package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.built.BuiltSubquery

class SubqueryExpr<T : Any>(
    val subquery: BuiltSubquery
): Expr<T>