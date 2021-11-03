package io.koalaql.query.fluent

import io.koalaql.Assignment
import io.koalaql.expr.*
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.SqlText
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface Selectable: QueryBodyBuilder, PerformableBlocking<RowSequence<ResultRow>> {
    private class Select(
        val of: Selectable,
        val references: List<SelectArgument>,
        val includeAll: Boolean
    ): Queryable<ResultRow> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            includeAll
        )

        override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(buildQuery())
    }

    private class SelectOne<A : Any>(
        val of: Selectable,
        val references: List<SelectArgument>
    ): QueryableOfOne<A> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )
    }

    private class SelectTwo<A : Any, B : Any>(
        val of: Selectable,
        val references: List<SelectArgument>
    ): QueryableOfTwo<A, B> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )
    }

    private class SelectThree<A : Any, B : Any, C : Any>(
        val of: Selectable,
        val references: List<SelectArgument>
    ): QueryableOfThree<A, B, C> {
        override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )
    }

    fun selectAll(vararg references: SelectArgument): Queryable<ResultRow> =
        Select(this, references.asList(), true)

    override fun generateSql(ds: SqlPerformer): SqlText? =
        selectAll().generateSql(ds)

    override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
        selectAll().performWith(ds)

    fun select(references: List<SelectArgument>): Queryable<ResultRow> =
        Select(this, references, false)

    fun select(vararg references: SelectArgument): Queryable<ResultRow> =
        Select(this, references.asList(), false)

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
        override fun buildUpdate() = BuiltUpdate(
            BuiltQueryBody.from(of),
            assignments
        )
    }

    fun update(assignments: List<Assignment<*>>): Updated =
        Update(this, assignments)

    fun update(vararg assignments: Assignment<*>): Updated =
        update(assignments.asList())

    private class Delete(
        val of: Selectable
    ): Deleted {
        override fun buildDelete() = BuiltDelete(BuiltQueryBody.from(of))
    }

    fun delete(): Deleted = Delete(this)
}

