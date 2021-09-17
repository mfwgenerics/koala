package io.koalaql.query.built

import io.koalaql.query.OnConflictAction
import io.koalaql.query.WithType
import io.koalaql.sql.Scope

class BuiltInsert: BuiltStatement {
    lateinit var relation: BuiltRelation

    var withType: WithType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    var onConflict: OnConflictAction? = null

    lateinit var query: BuiltSubquery

    override fun populateScope(scope: Scope) {

    }
}