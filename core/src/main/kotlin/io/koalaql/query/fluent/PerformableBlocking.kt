package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer

interface PerformableBlocking<out T>: PerformableSql {
    fun perform(ds: BlockingPerformer): T
}