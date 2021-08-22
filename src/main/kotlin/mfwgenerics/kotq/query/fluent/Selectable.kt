package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.Statementable
import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Subqueryable
import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSubquery

interface Selectable: BuildsIntoSelect {
    private class Select(
        val of: Selectable,
        val references: List<Labeled<*>>
    ): Subqueryable, BuildsIntoSelect {
        override fun buildQuery(): BuiltSubquery = buildSelect()

        override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
            out.selected = references
            out.columns = LabelList(references.map { it.name })
            return of
        }
    }

    fun select(vararg references: NamedExprs): Subqueryable =
        Select(this, references.asSequence().flatMap { it.namedExprs() }.toList())

    fun update(vararg assignments: Assignment<*>): Statementable =
        TODO()
}

