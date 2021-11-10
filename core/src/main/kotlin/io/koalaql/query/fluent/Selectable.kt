package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.dsl.as_
import io.koalaql.dsl.label
import io.koalaql.expr.*
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.SqlText
import io.koalaql.values.*

interface Selectable: BuildsIntoQueryBody, PerformableBlocking<RowSequence<ResultRow>> {
    private abstract class AnySelect<T>(
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): QueryableUnionOperand<T> {
        fun buildQuery(): BuiltUnionOperandQuery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            includeAll
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
    }

    private class Select(
        of: Selectable,
        references: List<SelectArgument>,
        includeAll: Boolean
    ): AnySelect<ResultRow>(of, references, includeAll), QueryableUnionOperand<ResultRow> {
        override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(BuiltQuery.from(this))

        override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<ResultRow> {
            override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
                ds.query(BuiltQuery.from(this))

            override fun BuiltQuery.buildInto(): QueryBuilder? {
                withType = type
                withs = queries

                return this@Select
            }
        }
    }

    private class EmptySelect(
        private val selectOne: QueryableOfOne<Int>
    ): QueryableUnionOperand<EmptyRow> {
        override fun BuiltQuery.buildInto(): QueryBuilder = selectOne

        override fun BuiltQuery.buildIntoQueryTail(type: SetOperationType, distinctness: Distinctness) =
            with (selectOne) { buildIntoQueryTail(type, distinctness) }

        override fun perform(ds: BlockingPerformer) =
            RowSequenceEmptyMask(ds.query(BuiltQuery.from(this)))

        override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<EmptyRow> {
            override fun perform(ds: BlockingPerformer): RowSequence<EmptyRow> =
                RowSequenceEmptyMask(ds.query(BuiltQuery.from(this)))

            override fun BuiltQuery.buildInto(): QueryBuilder? {
                withType = type
                withs = queries

                return this@EmptySelect
            }
        }
    }

    private class SelectOne<A : Any>(
        of: Selectable,
        references: List<SelectArgument>
    ): AnySelect<RowWithOneColumn<A>>(of, references, false), QueryableOfOne<A>

    private class SelectTwo<A : Any, B : Any>(
        of: Selectable,
        references: List<SelectArgument>
    ): AnySelect<RowWithTwoColumns<A, B>>(of, references, false), QueryableOfTwo<A, B>

    private class SelectThree<A : Any, B : Any, C : Any>(
        of: Selectable,
        references: List<SelectArgument>
    ): AnySelect<RowWithThreeColumns<A, B, C>>(of, references, false), QueryableOfThree<A, B, C>

    fun selectAll(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        Select(this, references.asList(), true)

    override fun generateSql(ds: SqlPerformer): SqlText? =
        selectAll().generateSql(ds)

    override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> =
        selectAll().perform(ds)

    fun select(references: List<SelectArgument>): QueryableUnionOperand<ResultRow> = if (references.isEmpty()) {
        EmptySelect(select(1 as_ label()))
    } else {
        Select(this, references, false)
    }

    fun select(vararg references: SelectArgument): QueryableUnionOperand<ResultRow> =
        select(references.asList())

    fun <A : Any> select(labeled: SelectOperand<A>): QueryableOfOne<A> =
        SelectOne(this, listOf(labeled))

    fun <A : Any, B : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>
    ): QueryableOfTwo<A, B> =
        SelectTwo(this, listOf(first, second))

    fun <A : Any, B : Any, C : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>,
        third: SelectOperand<C>
    ): QueryableOfThree<A, B, C> =
        SelectThree(this, listOf(first, second, third))

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
    ): Deleted {
        override fun BuiltDelete.buildInto(): BuildsIntoDelete? {
            query = BuiltQueryBody.from(of)
            return null
        }
    }

    fun delete(): Deleted = Delete(this)
}

