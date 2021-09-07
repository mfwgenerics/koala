package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.sql.Scope

class BuiltUpdate(
    val query: BuiltQueryBody,
    val assignments: List<Assignment<*>>
): BuiltStatement {
    override fun populateScope(scope: Scope) {
        query.populateScope(scope, false)
    }
}