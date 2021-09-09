package mfwgenerics.kotq.query.fluent

import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.query.Inserted
import mfwgenerics.kotq.query.Returning
import mfwgenerics.kotq.query.built.BuildsIntoInsert
import mfwgenerics.kotq.query.built.BuiltReturningInsert

interface Returningable: Inserted {
    private class InsertedReturning(
        val inserted: BuildsIntoInsert,
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