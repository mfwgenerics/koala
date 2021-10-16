package io.koalaql.query

import io.koalaql.query.built.BuiltRelation

object Tableless: RelationBuilder {
    override fun BuiltRelation.buildIntoRelation() {
        relation = EmptyRelation
        setAliases(null)
    }
}