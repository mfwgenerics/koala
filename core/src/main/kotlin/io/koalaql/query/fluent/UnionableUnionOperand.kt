package io.koalaql.query.fluent

import io.koalaql.expr.SelectArgument
import io.koalaql.expr.SelectOperand
import io.koalaql.expr.SelectedExpr
import io.koalaql.query.built.*

interface UnionableUnionOperand: Unionable, UnionOperand, QueryBodyBuilder {
    private class BuiltSelectedUnionOperand(
        val select: BuiltSelectQuery
    ): BuiltUnionOperand {
        override fun toSelectQuery(selected: List<SelectedExpr<*>>): BuiltSelectQuery =
            select.reorderToMatchUnion(selected)
    }

    private class SelectUnionableUnionOperand<T : Any>(
        val of: UnionableUnionOperand,
        val references: List<SelectArgument>
    ): SelectedUnionOperand, SelectedJustUnionOperand<T> {
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

    override fun select(vararg references: SelectArgument): SelectedUnionOperand =
        SelectUnionableUnionOperand<Nothing>(this, references.asList())

    override fun <T : Any> select(labeled: SelectOperand<T>): SelectedJustUnionOperand<T> =
        SelectUnionableUnionOperand(this, listOf(labeled))
}