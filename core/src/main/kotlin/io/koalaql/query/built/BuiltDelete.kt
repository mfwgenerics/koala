package io.koalaql.query.built

import io.koalaql.query.WithType
import io.koalaql.query.fluent.BuildsIntoDelete
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltDelete: BuiltStatement, BuiltWithable {
    lateinit var query: BuiltQueryBody

    override var withType = WithType.NOT_RECURSIVE
    override var withs: List<BuiltWith> = emptyList()

    override fun populateScope(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }

        query.populateScope(scope)
    }

    companion object {
        fun from(builder: BuildsIntoDelete): BuiltDelete =
            unfoldBuilder(builder, BuiltDelete()) { it.buildInto() }
    }
}