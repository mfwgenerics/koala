package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.sql.Scope

class BuiltDelete(
    val query: BuiltQueryBody
): BuiltStatement {
    override fun populateScope(scope: Scope) {
        query.populateScope(scope, false)
    }
}