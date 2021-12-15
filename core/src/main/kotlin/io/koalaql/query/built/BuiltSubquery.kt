package io.koalaql.query.built

import io.koalaql.expr.Reference

sealed interface BuiltSubquery: BuiltQueryable, BuiltDml {
    val columns: List<Reference<*>>

    fun columnsUnnamed(): Boolean
}