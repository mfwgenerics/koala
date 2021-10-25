package io.koalaql.query.fluent

import io.koalaql.query.BlockingPerformer
import io.koalaql.sql.SqlText

interface PerformableSql {
    fun generateSql(ds: BlockingPerformer): SqlText?
}