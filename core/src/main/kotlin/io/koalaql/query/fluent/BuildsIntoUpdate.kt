package io.koalaql.query.fluent

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltStatement
import io.koalaql.query.built.BuiltUpdate

fun interface BuildsIntoUpdate: PerformableStatement {
    fun BuiltUpdate.buildInto(): BuildsIntoUpdate?

    override fun BuilderContext.buildStmt(): BuiltStatement =
        BuiltUpdate.from(this@BuildsIntoUpdate)
}