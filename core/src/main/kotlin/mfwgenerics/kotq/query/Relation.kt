package mfwgenerics.kotq.query

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.expr.RelvarColumn
import mfwgenerics.kotq.query.built.BuiltRelation
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.query.built.BuiltValuesQuery
import mfwgenerics.kotq.values.RowIterator
import mfwgenerics.kotq.values.RowSequence

sealed interface Relation: AliasedRelation {
    fun as_(alias: Alias): AliasedRelation = Aliased(this, alias)

    override fun buildQueryRelation(): BuiltRelation
        = BuiltRelation(this, null)
}

interface Relvar: Relation {
    val relvarName: String

    val columns: List<RelvarColumn<*>>
}

class Subquery(
    val of: BuiltSubquery
): Relation

class Values(
    override val columns: LabelList,
    private val impl: () -> RowIterator
): Relation, Subqueryable, RowSequence {
    override fun rowIterator(): RowIterator = impl()
    override fun buildQuery(): BuiltSubquery = BuiltValuesQuery(this)
}

class Cte(
    val identifier: IdentifierName = IdentifierName()
): Relation {
    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}