package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.OnConflictAction
import mfwgenerics.kotq.query.WithType
import mfwgenerics.kotq.sql.Scope

class BuiltInsert: BuiltStatement {
    lateinit var relation: BuiltRelation

    var withType: WithType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    var onConflict: OnConflictAction? = null

    lateinit var query: BuiltSubquery

    override fun populateScope(scope: Scope) {

    }
}