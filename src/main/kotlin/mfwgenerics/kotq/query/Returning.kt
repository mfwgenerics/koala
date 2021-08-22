package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuiltReturningInsert

interface Returning: Queryable {
    override fun buildQuery(): BuiltReturningInsert
}