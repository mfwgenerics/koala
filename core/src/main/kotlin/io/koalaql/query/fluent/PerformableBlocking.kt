package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer

interface PerformableBlocking<T>: PerformableSql {
    fun performWith(ds: BlockingPerformer): T
}