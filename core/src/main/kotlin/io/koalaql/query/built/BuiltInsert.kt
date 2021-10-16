package io.koalaql.query.built

import io.koalaql.query.OnConflictOrDuplicateAction
import io.koalaql.query.Relvar
import io.koalaql.query.WithType
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltInsert: BuiltStatement {
    lateinit var relation: BuiltRelation

    var ignore: Boolean = false

    var withType: WithType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    var onConflict: OnConflictOrDuplicateAction? = null

    lateinit var query: BuiltSubquery

    fun unwrapTable(): Relvar = when (val relation = relation.relation) {
        is Relvar -> relation
        else -> error("can't insert into something that isn't a table")
    }

    override fun populateScope(scope: Scope) {

    }

    companion object {
        fun from(builder: InsertBuilder): BuiltInsert =
            unfoldBuilder(builder, BuiltInsert()) { it.buildIntoInsert() }
    }
}