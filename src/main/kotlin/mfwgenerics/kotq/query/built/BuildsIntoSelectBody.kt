package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoSelectBody: BuildsIntoSelect {
    fun buildSelectBody(): BuiltSelectBody =
        unfoldBuilder(BuiltSelectBody()) { buildIntoSelectBody(it) }

    fun buildIntoSelectBody(out: BuiltSelectBody): BuildsIntoSelectBody?

    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? =
        buildIntoSelectBody(out.body)
}