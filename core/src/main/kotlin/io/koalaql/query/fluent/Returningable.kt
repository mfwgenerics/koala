package io.koalaql.query.fluent

import io.koalaql.expr.Reference
import io.koalaql.query.Inserted
import io.koalaql.query.Returning
import io.koalaql.query.built.BuildsIntoInsert
import io.koalaql.query.built.BuiltReturningInsert

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