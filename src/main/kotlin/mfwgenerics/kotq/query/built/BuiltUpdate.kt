package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.sql.Scope

class BuiltUpdate: BuiltStatement {
    lateinit var select: BuiltSelectQuery

    var assignments: List<Assignment<*>> = emptyList()

    override fun populateScope(scope: Scope) {
        select.populateScope(scope)
    }
}