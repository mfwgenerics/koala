package io.koalaql.query.built

import io.koalaql.sql.Scope

sealed interface BuiltStatement {
    fun populateScope(scope: Scope)
}