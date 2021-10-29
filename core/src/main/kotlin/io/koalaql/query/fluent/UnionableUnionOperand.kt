package io.koalaql.query.fluent

import io.koalaql.expr.*
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.built.*
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

interface UnionableUnionOperand: Unionable, UnionOperand, QueryBodyBuilder {
    private class BuiltSelectedUnionOperand(
        val select: BuiltSelectQuery
    ): BuiltUnionOperand {
        override fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery =
            select.reorderToMatchUnion(selected)
    }

    private class SelectUnionableUnionOperand(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): QueryableUnionOperand {
        override fun buildQuery() = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )

        override fun BuiltSetOperation.buildIntoSetOperation() {
            body = BuiltSelectedUnionOperand(buildQuery())
        }

        override fun performWith(ds: BlockingPerformer): RowSequence<ResultRow> =
            ds.query(buildQuery())
    }

    private class SelectUnionableUnionOperandOfOne<T : Any>(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): QueryableOfOneUnionOperand<T> {
        override fun buildQuery() = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )

        override fun BuiltSetOperation.buildIntoSetOperation() {
            body = BuiltSelectedUnionOperand(buildQuery())
        }
    }

    private class SelectUnionableUnionOperandOfTwo<A : Any, B : Any>(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): QueryableOfTwoUnionOperand<A, B> {
        override fun buildQuery() = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )

        override fun BuiltSetOperation.buildIntoSetOperation() {
            body = BuiltSelectedUnionOperand(buildQuery())
        }
    }

    private class SelectUnionableUnionOperandOfThree<A : Any, B : Any, C : Any>(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): QueryableOfThreeUnionOperand<A, B, C> {
        override fun buildQuery() = BuiltSelectQuery(
            BuiltQueryBody.from(of),
            references,
            false
        )

        override fun BuiltSetOperation.buildIntoSetOperation() {
            body = BuiltSelectedUnionOperand(buildQuery())
        }
    }

    private class BuiltQueryUnionOperand(
        val query: BuiltQueryBody
    ): BuiltUnionOperand {
        override fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery =
            BuiltSelectQuery(query, selected)
    }

    override fun BuiltSetOperation.buildIntoSetOperation() {
        body = BuiltQueryUnionOperand(BuiltQueryBody.from(this@UnionableUnionOperand))
    }

    override fun select(vararg references: SelectArgument): QueryableUnionOperand =
        SelectUnionableUnionOperand(this, references.asList())

    override fun <T : Any> select(labeled: SelectOperand<T>): QueryableOfOneUnionOperand<T> =
        SelectUnionableUnionOperandOfOne(this, listOf(labeled))

    override fun <A : Any, B : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>
    ): QueryableOfTwoUnionOperand<A, B> =
        SelectUnionableUnionOperandOfTwo(this, listOf(first, second))

    override fun <A : Any, B : Any, C : Any> select(
        first: SelectOperand<A>,
        second: SelectOperand<B>,
        third: SelectOperand<C>
    ): QueryableOfThreeUnionOperand<A, B, C> =
        SelectUnionableUnionOperandOfThree(this, listOf(first, second, third))
}