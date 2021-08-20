package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.dsl.WithType
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.sql.Scope

class BuiltWhere {
    lateinit var relation: BuiltRelation

    var withType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    val joins: MutableList<BuiltJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { it.populateScope(scope) }

        withs.forEach {
            val innerScope = scope.innerScope()

            it.query.populateScope(innerScope)

            innerScope.externals().forEach { name ->
                scope.internal(it.alias[name], name, it.alias)
            }

            scope.register(it.alias, innerScope)
        }
    }
}