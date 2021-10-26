package io.koalaql.query.built

import io.koalaql.query.*
import io.koalaql.sql.Scope

class BuiltRelation {
    lateinit var relation: Relation

    var explicitAlias: Alias? = null
    lateinit var computedAlias: Alias

    fun setAliases(explicitAlias: Alias?, defaultAlias: Alias? = null) {
        this.explicitAlias = explicitAlias
        computedAlias = explicitAlias?:defaultAlias?:Alias()
    }

    fun populateScope(scope: Scope) {
        val names = when (val relation = relation) {
            is TableRelation -> relation.columns.map { it to it.symbol }
            is Subquery -> relation.of.columns.map { it to scope.names[it] }
            is Cte -> scope.cteColumns(relation).map { it to scope.names[it] }
            is Values -> relation.columns.map { it to scope.names[it] }
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

    companion object {
        fun from(builder: RelationBuilder): BuiltRelation = with (builder) {
            BuiltRelation().also { it.buildIntoRelation() }
        }
    }
}