package io.koalaql.query

import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltInsert
import io.koalaql.query.built.BuiltStatement
import io.koalaql.query.built.InsertBuilder
import io.koalaql.query.fluent.PerformableStatement

interface Inserted: PerformableStatement, InsertBuilder {
    override fun BuilderContext.buildStmt(): BuiltStatement =
        BuiltInsert.from(this@Inserted)
}