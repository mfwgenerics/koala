package io.koalaql.query.built

import io.koalaql.query.OnConflictAction
import io.koalaql.query.Relvar
import io.koalaql.query.WithType
import io.koalaql.sql.Scope

class BuiltInsert: BuiltStatement {
    lateinit var relation: BuiltRelation

    var ignore: Boolean = false

    var withType: WithType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    var onConflict: OnConflictAction? = null

    lateinit var query: BuiltSubquery

    fun unwrapTable(): Relvar = when (val relation = relation.relation) {
        is Relvar -> relation
        else -> error("can't insert into something that isn't a table")
    }

    override fun populateScope(scope: Scope) {

    }
}