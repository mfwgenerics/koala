package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.built.BuiltSubquery

class SubqueryExpr(
    val subquery: BuiltSubquery
): QuasiExpr {
}