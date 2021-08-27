package mfwgenerics.kotq.query

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.RelvarColumn
import mfwgenerics.kotq.query.built.BuiltRelation
import mfwgenerics.kotq.query.built.BuiltSubquery

sealed interface Relation: AliasedRelation {
    fun alias(alias: Alias): AliasedRelation = Aliased(this, alias)

    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(this, null)
}

interface Relvar: Relation {
    val relvarName: String

    val columns: List<RelvarColumn<*>>
}

class Subquery(
    val of: BuiltSubquery
): Relation {
    override fun namedExprs(): List<SelectedExpr<*>> {
        error("not implemented")
    }
}

class Cte(
    val identifier: IdentifierName = IdentifierName()
): Relation {
    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"

    override fun namedExprs(): List<SelectedExpr<*>> {
        error("not implemented")
    }
}