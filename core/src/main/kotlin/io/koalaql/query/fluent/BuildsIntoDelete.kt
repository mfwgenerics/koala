package io.koalaql.query.fluent

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltDelete
import io.koalaql.query.built.BuiltStatement

fun interface BuildsIntoDelete: PerformableStatement {
    fun BuiltDelete.buildInto(): BuildsIntoDelete?

    override fun BuilderContext.buildStmt(): BuiltStatement =
        BuiltDelete.from(this@BuildsIntoDelete)
}