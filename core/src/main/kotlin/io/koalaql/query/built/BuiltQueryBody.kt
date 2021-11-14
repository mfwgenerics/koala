package io.koalaql.query.built

import io.koalaql.expr.Expr
import io.koalaql.expr.Ordinal
import io.koalaql.query.LockMode
import io.koalaql.query.BackwardsList
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder
import io.koalaql.window.LabeledWindow

class BuiltQueryBody {
    lateinit var relation: BuiltRelation

    val joins: BackwardsList<BuiltJoin> = BackwardsList()

    var where: Expr<Boolean>? = null

    var groupBy: List<Expr<*>> = arrayListOf()
    var having: Expr<Boolean>? = null

    var windows: List<LabeledWindow> = emptyList()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    var locking: LockMode? = null

    fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { join ->
            join.populateScope(scope)
        }
    }

    companion object {
        fun from(builder: BuildsIntoQueryBody): BuiltQueryBody =
            unfoldBuilder(builder, BuiltQueryBody()) { it.buildInto() }
    }
}