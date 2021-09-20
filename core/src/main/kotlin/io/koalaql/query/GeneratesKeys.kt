package io.koalaql.query

import io.koalaql.query.built.BuiltGeneratesKeysInsert

interface GeneratesKeys: Queryable {
    override fun buildQuery(): BuiltGeneratesKeysInsert
}