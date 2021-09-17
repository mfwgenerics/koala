package io.koalaql.query

import io.koalaql.query.built.BuiltReturningInsert

interface Returning: Queryable {
    override fun buildQuery(): BuiltReturningInsert
}