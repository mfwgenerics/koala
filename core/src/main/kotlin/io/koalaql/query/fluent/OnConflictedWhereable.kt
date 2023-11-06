package io.koalaql.query.fluent

import io.koalaql.expr.Expr

interface OnConflictedWhereable: OnConflicted {
    fun where(where: Expr<Boolean>): OnConflicted
}