package io.koalaql.query

import io.koalaql.IdentifierName
import io.koalaql.expr.RelvarColumn
import io.koalaql.query.built.BuiltRelation
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.query.built.BuiltValuesQuery
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence
import io.koalaql.values.ValuesRow

sealed interface Relation: AliasableRelation {
    override fun as_(alias: Alias): Aliased = Aliased(this, alias)

    override fun BuiltRelation.buildIntoRelation() {
        relation = this@Relation
        setAliases(null)
    }
}

object EmptyRelation: Relation

interface Relvar: Relation {
    val tableName: String

    val columns: List<RelvarColumn<*>>
}

class Subquery(
    val of: BuiltSubquery
): Relation

class Values(
    override val columns: LabelList,
    private val impl: () -> RowIterator<ValuesRow>
): Relation, Subqueryable, RowSequence<ValuesRow> {
    override fun rowIterator(): RowIterator<ValuesRow> = impl()
    override fun buildQuery(): BuiltSubquery = BuiltValuesQuery(this)
}

class Cte(
    val identifier: IdentifierName = IdentifierName()
): Relation {
    override fun BuiltRelation.buildIntoRelation() {
        relation = this@Cte
        setAliases(null, Alias(identifier))
    }

    override fun equals(other: Any?): Boolean =
        other is Alias && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}