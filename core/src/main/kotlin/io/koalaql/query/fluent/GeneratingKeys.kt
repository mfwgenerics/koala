package io.koalaql.query.fluent

import io.koalaql.expr.Column
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.GeneratingKey
import io.koalaql.query.Inserted
import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuiltGeneratesKeysInsert
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.InsertBuilder
import io.koalaql.sql.CompiledSql
import io.koalaql.values.RowIterator
import io.koalaql.values.RowSequence

interface GeneratingKeys: Inserted {
    private class InsertedGeneratingKey<T : Any>(
        val inserted: InsertBuilder,
        val returning: Column<T>
    ): GeneratingKey<T> {
        override fun buildQuery(): BuiltGeneratesKeysInsert {
            val built = BuiltInsert.from(inserted)

            return BuiltGeneratesKeysInsert(
                built,
                returning
            )
        }

        override fun perform(ds: BlockingPerformer): RowSequence<T> {
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

        override fun generateSql(ds: SqlPerformer): CompiledSql? = ds.generateSql(BuiltInsert.from(inserted))
    }

    fun <T : Any> generatingKey(reference: Column<T>): GeneratingKey<T> =
        InsertedGeneratingKey(this, reference)
}