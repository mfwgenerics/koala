package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.query.Returning
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltReturningInsert

interface Inserted: BuildsIntoInsert {
    private class InsertedReturning(
        val inserted: Inserted,
        val returning: List<SelectedExpr<*>> = emptyList()
    ): Returning {
        override fun buildQuery(): BuiltReturningInsert = BuiltReturningInsert(
            inserted.buildInsert(),
            returning
        )
    }

    fun returning(vararg references: NamedExprs): Returning =
        InsertedReturning(this, references.flatMap { it.namedExprs() })
}