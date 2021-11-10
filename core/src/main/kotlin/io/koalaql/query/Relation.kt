package io.koalaql.query

import io.koalaql.IdentifierName
import io.koalaql.expr.Column
import io.koalaql.query.built.*
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.sql.SqlText
import io.koalaql.values.ResultRow
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

interface TableRelation: Relation {
    val tableName: String

    val columns: List<Column<*>>
}

class Subquery(
    val of: BuiltFullQuery
): Relation

class Values(
    override val columns: LabelList,
    private val impl: () -> RowIterator<ValuesRow>
): QueryableUnionOperand<ResultRow>, RowSequence<ValuesRow> {
    override fun rowIterator(): RowIterator<ValuesRow> = impl()

    override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder? {
        head = BuiltValuesQuery(this@Values)
        return null
    }

    override fun BuiltFullQuery.buildIntoFullQueryTail(type: SetOperationType, distinctness: Distinctness) {
        this.unioned.add(BuiltFullSetOperation(
            type = type,
            distinctness = distinctness,
            body = BuiltValuesQuery(this@Values)
        ))
    }

    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
        ds.query(BuiltFullQuery.from(this))
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