package mfwgenerics.kotq.query

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.RelvarColumn
import mfwgenerics.kotq.expr.SelectionBuilder
import mfwgenerics.kotq.query.built.BuiltRelation
import mfwgenerics.kotq.query.built.BuiltSubquery

sealed interface Relation: AliasedRelation {
    fun alias(alias: Alias): AliasedRelation = Aliased(this, alias)

    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(this, null)

    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.fromRelation(this)
    }
}

interface Relvar: Relation {
    val relvarName: String

    val columns: List<RelvarColumn<*>>
}

class Subquery(
    val of: BuiltSubquery
): Relation

class Cte(
    val identifier: IdentifierName = IdentifierName()
): Relation {
    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}