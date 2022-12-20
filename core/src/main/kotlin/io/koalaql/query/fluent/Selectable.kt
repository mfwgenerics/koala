package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.dsl.as_
import io.koalaql.dsl.label
import io.koalaql.expr.ExprQueryableUnionOperand
import io.koalaql.expr.Reference
import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectOperand
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.values.*

interface Selectable: BuildsIntoQueryBody, QueryableUnionOperand<ResultRow> {
    override fun BuiltQuery.buildInto(): QueryBuilder? =
        with (selectAll()) { buildInto() }

    override fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness) {
        with (selectAll()) { buildIntoQueryTail(type, distinctness) }
    }

    override fun with(type: WithType, queries: List<BuiltWith>): Queryable<ResultRow> =
        selectAll().with(type, queries)

    private class Select(
        val distinctness: Distinctness,
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): QueryableUnionOperand<ResultRow> {
        init {
            val checkedReferences = hashSetOf<Reference<*>>()

            references.forEach {
                with (it) { checkedReferences.enforceUniqueReference() }
            }
        }

        fun buildQuery(): BuiltUnionOperandQuery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            includeAll,
            distinctness
        )

        override fun BuiltQuery.buildInto(): QueryBuilder? {
            head = buildQuery()
            return null
        }

        override fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness) {
            unioned.add(BuiltSetOperation(
                type = type,
                distinctness = distinctness,
                body = buildQuery()
            ))
        }

        override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(with (this) { BuilderContext.buildQuery() })

        override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<ResultRow> {
            override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
                ds.query(with (this) { BuilderContext.buildQuery() })

            override fun BuiltQuery.buildInto(): QueryBuilder? {
                withType = type
                withs = queries

                return this@Select
            }
        }
    }

    private fun select(
        distinctness: Distinctness,
        references: List<SelectArgument>
    ): QueryableUnionOperand<ResultRow> = if (references.isEmpty()) {
        val x = label<Int>()

        EmptyQueryableUnionOperand(select(distinctness, listOf(1 as_ x)))
    } else {
        Select(distinctness, this, references, false)
    }

    fun select(references: List<SelectArgument>): QueryableUnionOperand<ResultRow> =
        select(Distinctness.ALL, references)

    fun selectDistinct(references: List<SelectArgument>): QueryableUnionOperand<ResultRow> =
        select(Distinctness.DISTINCT, references)

    fun selectAll(references: List<SelectArgument>): QueryableUnionOperand<ResultRow> =
        Select(Distinctness.ALL, this, references, true)

    fun selectAll(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        selectAll(references.asList())

    override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
        selectAll().perform(ds)

    private class EmptyQueryableUnionOperand(
        of: QueryableUnionOperand<*>
    ): CastQueryableUnionOperand<ResultRow>(
        of,
        { RowSequenceEmptyMask(it) }
    ), QueryableUnionOperand<ResultRow>

    fun select(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        select(references.asList())

    fun selectDistinct(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        selectDistinct(references.asList())

    fun selectDistinctAll(references: List<SelectArgument>): QueryableUnionOperand<ResultRow> =
        Select(Distinctness.DISTINCT, this, references, true)

    fun selectDistinctAll(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        selectDistinctAll(references.asList())

    fun <A : Any> select(labeled: SelectOperand<A>): ExprQueryableUnionOperand<A> =
        CastExprQueryableUnionOperand(select(listOf(labeled))) {
            it.unsafeCastToOneColumn()
        }

    fun <A : Any, B : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>
    ): QueryableUnionOperand<RowOfTwo<A, B>> =
        CastQueryableUnionOperand(select(listOf(first, second))) {
            it.unsafeCastToTwoColumns()
        }

    fun <A : Any, B : Any, C : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>,
        third: SelectOperand<C>
    ): QueryableUnionOperand<RowOfThree<A, B, C>> =
        CastQueryableUnionOperand(select(listOf(first, second, third))) {
            it.unsafeCastToThreeColumns()
        }

    fun <A : Any> selectDistinct(labeled: SelectOperand<A>): ExprQueryableUnionOperand<A> =
        CastExprQueryableUnionOperand(selectDistinct(listOf(labeled))) {
            it.unsafeCastToOneColumn()
        }

    fun <A : Any, B : Any> selectDistinct(
        first: SelectOperand<A>,
        second: SelectOperand<B>
    ): QueryableUnionOperand<RowOfTwo<A, B>> =
        CastQueryableUnionOperand(selectDistinct(listOf(first, second))) {
            it.unsafeCastToTwoColumns()
        }

    fun <A : Any, B : Any, C : Any> selectDistinct(
        first: SelectOperand<A>,
        second: SelectOperand<B>,
        third: SelectOperand<C>
    ): QueryableUnionOperand<RowOfThree<A, B, C>> =
        CastQueryableUnionOperand(selectDistinct(listOf(first, second, third))) {
            it.unsafeCastToThreeColumns()
        }

    private class Update(
        val of: Selectable,
        val assignments: List<Assignment<*>>
    ): Updated {
        override fun BuiltUpdate.buildInto(): BuildsIntoUpdate? {
            query = BuiltQueryBody.from(of)
            assignments = this@Update.assignments

            return null
        }
    }

    fun update(assignments: List<Assignment<*>>): Updated =
        Update(this, assignments)

    fun update(vararg assignments: Assignment<*>): Updated =
        update(assignments.asList())

    private class Delete(
        val of: Selectable
    ): WithableDelete {
        override fun BuiltDelete.buildInto(): BuildsIntoDelete? {
            query = BuiltQueryBody.from(of)
            return null
        }
    }

    fun delete(): WithableDelete = Delete(this)
}

