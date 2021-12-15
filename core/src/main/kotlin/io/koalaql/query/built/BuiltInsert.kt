package io.koalaql.query.built

import io.koalaql.identifier.Named
import io.koalaql.query.*
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltInsert: BuiltStatement {
    lateinit var relation: BuiltRelation

    var ignore: Boolean = false

    var onConflict: OnConflictOrDuplicateAction? = null

    lateinit var query: BuiltQuery

    fun unwrapTable(): TableRelation = when (val relation = relation.relation) {
        is TableRelation -> relation
        else -> error("can't insert into something that isn't a table")
    }

    override fun populateScope(scope: Scope) {
        val columns = unwrapTable().columns

        columns.forEach {
            scope.internal(it, Named(it.symbol), null)
        }
    }

    companion object {
        fun from(builder: InsertBuilder): BuiltInsert =
            unfoldBuilder(builder, BuiltInsert()) { it.buildIntoInsert() }
    }
}