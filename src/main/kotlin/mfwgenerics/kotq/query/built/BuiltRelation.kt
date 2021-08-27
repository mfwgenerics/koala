package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.query.Relation
import mfwgenerics.kotq.query.Relvar
import mfwgenerics.kotq.query.Subquery
import mfwgenerics.kotq.sql.Scope

data class BuiltRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?: Alias()

    fun populateScope(scope: Scope) {
        when (relation) {
            is Relvar -> {
                relation.columns.forEach { column ->
                    scope.internal(
                        alias?.get(column)?:column,
                        column.symbol,
                        computedAlias
                    )
                }
            }
            is Subquery -> {
                relation.of.exports().forEach { name ->
                    if (alias == null) {
                        scope.internal(name, scope.names[name], computedAlias)
                    } else {
                        scope.internal(computedAlias[name], scope.names[name], computedAlias)
                    }
                }
            }
        }
    }
}