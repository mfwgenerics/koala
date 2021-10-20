package io.koalaql.query.fluent

import io.koalaql.expr.Reference
import io.koalaql.query.GeneratesKeys
import io.koalaql.query.Inserted
import io.koalaql.query.built.BuiltGeneratesKeysInsert
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder

interface Returningable: Inserted {
    private class InsertedGeneratesKeys(
        val inserted: InsertBuilder,
        val returning: Reference<*>
    ): GeneratesKeys {
        override fun buildQuery(): BuiltGeneratesKeysInsert {
            val built = BuiltInsert.from(inserted)

            return BuiltGeneratesKeysInsert(
                built,
                returning
            )
        }
    }

    fun generatingKeys(reference: Reference<*>): GeneratesKeys =
        InsertedGeneratesKeys(this, reference)
}