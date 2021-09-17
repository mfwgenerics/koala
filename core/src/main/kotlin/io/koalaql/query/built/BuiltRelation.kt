package io.koalaql.query.built

import io.koalaql.query.*
import io.koalaql.sql.Scope

class BuiltRelation(
    val relation: Relation,
    val explicitAlias: Alias?,
    defaultAlias: Alias? = null
) {
    val computedAlias = explicitAlias?:defaultAlias?:Alias()

    fun populateScope(scope: Scope) {
        val names = when (relation) {
            is Relvar -> relation.columns.map { it to it.symbol }
            is Subquery -> relation.of.columns.values.map { it to scope.names[it] }
            is Cte -> scope.cteColumns(relation).values.map { it to scope.names[it] }
            is Values -> relation.columns.values.map { it to scope.names[it] }
            is EmptyRelation -> return
        }

        names.forEach { (name, symbol) ->
            scope.internal(
                explicitAlias?.get(name)?:name,
                symbol,
                computedAlias
            )
        }
    }
}