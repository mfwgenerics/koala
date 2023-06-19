package io.koalaql.query.built

import io.koalaql.Assignment
import io.koalaql.query.WithType
import io.koalaql.query.fluent.BuildsIntoUpdate
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltUpdate: BuiltStatement, BuiltWithable {
    lateinit var query: BuiltQueryBody
    var assignments: List<Assignment<*>> = emptyList()

    override var withType = WithType.NOT_RECURSIVE
    override var withs: List<BuiltWith> = emptyList()

    override fun populateScope(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }

        query.populateScope(scope)
    }

    companion object {
        fun from(builder: BuildsIntoUpdate): BuiltUpdate =
            unfoldBuilder(builder, BuiltUpdate()) { it.buildInto() }
    }
}