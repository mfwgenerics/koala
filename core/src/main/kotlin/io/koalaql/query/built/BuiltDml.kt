package io.koalaql.query.built

import io.koalaql.sql.Scope

sealed interface BuiltDml {
    fun populateScope(scope: Scope)
}