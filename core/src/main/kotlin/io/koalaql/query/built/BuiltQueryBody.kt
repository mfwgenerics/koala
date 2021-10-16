package io.koalaql.query.built

import io.koalaql.expr.Expr
import io.koalaql.expr.Ordinal
import io.koalaql.query.LockMode
import io.koalaql.query.WithType
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder
import io.koalaql.window.LabeledWindow

class BuiltQueryBody {
    lateinit var relation: BuiltRelation

    var withType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    val joins: MutableList<BuiltJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    var groupBy: List<Expr<*>> = arrayListOf()
    var having: Expr<Boolean>? = null

    var windows: List<LabeledWindow> = emptyList()

    val setOperations: MutableList<BuiltSetOperation> = arrayListOf()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    var locking: LockMode? = null

    fun populateScope(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }

        relation.populateScope(scope)

        joins.forEach { join ->
            join.populateScope(scope)
        }
    }

    companion object {
        fun from(builder: QueryBodyBuilder): BuiltQueryBody =
            unfoldBuilder(builder, BuiltQueryBody()) { it.buildIntoQueryBody() }
    }
}