package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.dsl.Relation
import mfwgenerics.kotq.dsl.Relvar
import mfwgenerics.kotq.dsl.Subquery
import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.sql.Scope

data class BuiltRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?: Alias()

    fun populateScope(scope: Scope) {
        when (relation) {
            is Relvar -> {
                val innerScope = scope.innerScope()

                relation.columns.forEach {
                    innerScope.external(it, it.symbol)
                }

                innerScope.externals().forEach { name ->
                    if (alias == null) {
                        scope.internal(name, name, computedAlias)
                    } else {
                        scope.internal(computedAlias[name], name, computedAlias)
                    }
                }

                scope.register(computedAlias, innerScope)
            }
            is Subquery -> {
                val innerScope = scope.innerScope()

                relation.of.populateScope(innerScope)

                innerScope.externals().forEach { name ->
                    if (alias == null) {
                        scope.internal(name, name, computedAlias)
                    } else {
                        scope.internal(computedAlias[name], name, computedAlias)
                    }
                }

                scope.register(computedAlias, innerScope)
            }
        }
    }
}