package io.koalaql.query

import io.koalaql.IdentifierName
import io.koalaql.expr.Column
import io.koalaql.query.built.*
import io.koalaql.query.fluent.QueryableResultsUnionOperand
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.*

sealed interface Relation: AliasableRelation {
    override fun as_(alias: Alias): Aliased = Aliased(this, alias)

    override fun BuiltRelation.buildIntoRelation() {
        relation = this@Relation
        setAliases(null)
    }
}

object EmptyRelation: Relation

interface TableRelation: Relation {
    val tableName: String

    val columns: List<Column<*>>
}

class Subquery(
    val of: BuiltQuery
): Relation

class Values(
    override val columns: LabelList,
    private val impl: () -> ValuesIterator
): QueryableResultsUnionOperand, ValuesSequence {
    override fun valuesIterator(): ValuesIterator = impl()

    override fun BuiltQuery.buildInto(): QueryBuilder? {
        head = BuiltValuesQuery(this@Values)
        return null
    }

    override fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness) {
        this.unioned.add(BuiltSetOperation(
            type = type,
            distinctness = distinctness,
            body = BuiltValuesQuery(this@Values)
        ))
    }

    override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
        ds.query(BuiltQuery.from(this))

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<ResultRow> {
        override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(BuiltQuery.from(this))

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@Values
        }
    }
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