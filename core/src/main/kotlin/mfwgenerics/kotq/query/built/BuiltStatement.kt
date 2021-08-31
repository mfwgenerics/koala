package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.sql.Scope

sealed interface BuiltStatement {
    fun populateScope(scope: Scope)
}