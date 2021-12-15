package io.koalaql.expr

import io.koalaql.query.built.BuiltSubquery

class SubqueryQuasiExpr(
    val query: BuiltSubquery
): QuasiExpr