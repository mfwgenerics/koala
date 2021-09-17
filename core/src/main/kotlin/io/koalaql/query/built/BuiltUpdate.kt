package io.koalaql.query.built

import io.koalaql.Assignment
import io.koalaql.sql.Scope

class BuiltUpdate(
    val query: BuiltQueryBody,
    val assignments: List<Assignment<*>>
): BuiltStatement {
    override fun populateScope(scope: Scope) {
        query.populateScope(scope)
    }
}