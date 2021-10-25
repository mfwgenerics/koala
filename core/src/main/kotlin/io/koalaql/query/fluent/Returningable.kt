package io.koalaql.query.fluent

import io.koalaql.expr.RelvarColumn
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.GeneratingKey
import io.koalaql.query.Inserted
import io.koalaql.query.LabelList
import io.koalaql.query.built.BuiltGeneratesKeysInsert
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence

interface Returningable: Inserted {
    private class InsertedGeneratingKey<T : Any>(
        val inserted: InsertBuilder,
        val returning: RelvarColumn<T>
    ): GeneratingKey<T> {
        override fun buildQuery(): BuiltGeneratesKeysInsert {
            val built = BuiltInsert.from(inserted)

            return BuiltGeneratesKeysInsert(
                built,
                returning
            )
        }

        override fun performWith(ds: BlockingPerformer): RowSequence<T> {
            val rows = ds.query(buildQuery())

            return object : RowSequence<T> {
                override val columns get() = rows.columns

                override fun rowIterator(): RowIterator<T> {
                    val it = rows.rowIterator()

                    return object : RowIterator<T> {
                        override val row: T get() = it.row.getValue(returning)
                        override fun takeRow(): T = row

                        override fun next(): Boolean = it.next()

                        override fun close() = it.close()
                    }
                }
            }
        }
    }

    fun <T : Any> generatingKey(reference: RelvarColumn<T>): GeneratingKey<T> =
        InsertedGeneratingKey(this, reference)
}