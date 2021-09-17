package io.koalaql.query.built

import io.koalaql.sql.Scope

class BuiltDelete(
    val query: BuiltQueryBody
): BuiltStatement {
    override fun populateScope(scope: Scope) {
        query.populateScope(scope)
    }
}