package io.koalaql.query

import io.koalaql.query.built.BuiltUpdate

interface Updated: PerformableStatement {
    fun buildUpdate(): BuiltUpdate
}