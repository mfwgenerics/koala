package io.koalaql.expr

import io.koalaql.query.built.BuiltQuery

class SubqueryQuasiExpr(
    val query: BuiltQuery
): QuasiExpr