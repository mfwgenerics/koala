package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.Returning
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltReturningInsert

interface Inserted: BuildsIntoInsert {
    private class InsertedReturning(
        val inserted: Inserted,
        val returning: List<Reference<*>> = emptyList()
    ): Returning {
        override fun buildQuery(): BuiltReturningInsert {
            val built = inserted.buildInsert()

            return BuiltReturningInsert(
                built,
                returning
            )
        }
    }

    fun returning(vararg references: Reference<*>): Returning =
        InsertedReturning(this, references.asList())
}