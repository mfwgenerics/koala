package io.koalaql.query

import io.koalaql.query.built.BuiltGeneratesKeysInsert
import io.koalaql.query.fluent.PerformableBlocking
import io.koalaql.values.RowSequence

interface GeneratingKey<T>: PerformableBlocking<RowSequence<T>> {
    fun buildQuery(): BuiltGeneratesKeysInsert

    override fun performWith(ds: BlockingPerformer): RowSequence<T>
}