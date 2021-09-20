package io.koalaql.query.fluent

import io.koalaql.expr.Reference
import io.koalaql.query.Inserted
import io.koalaql.query.GeneratesKeys
import io.koalaql.query.built.BuildsIntoInsert
import io.koalaql.query.built.BuiltGeneratesKeysInsert

interface Returningable: Inserted {
    private class InsertedGeneratesKeys(
        val inserted: BuildsIntoInsert,
        val returning: Reference<*>
    ): GeneratesKeys {
        override fun buildQuery(): BuiltGeneratesKeysInsert {
            val built = inserted.buildInsert()

            return BuiltGeneratesKeysInsert(
                built,
                returning
            )
        }
    }

    fun generatingKeys(reference: Reference<*>): GeneratesKeys =
        InsertedGeneratesKeys(this, reference)
}