package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.Scope

data class BuiltRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?: Alias()

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
                alias?.get(name)?:name,
                symbol,
                computedAlias
            )
        }
    }
}