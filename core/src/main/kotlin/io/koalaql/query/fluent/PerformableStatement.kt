package io.koalaql.query.fluent

import io.koalaql.expr.SelectOperand
import io.koalaql.query.BlockingPerformer
import io.koalaql.query.ExpectableSubqueryable
import io.koalaql.query.ReturningQueryable
import io.koalaql.query.SqlPerformer
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltStatement
import io.koalaql.sql.CompiledSql
import io.koalaql.values.ResultRow

interface PerformableStatement: PerformableBlocking<Int>, Returningable {
    fun BuilderContext.buildStmt(): BuiltStatement

    override fun returning(references: List<SelectOperand<*>>): ExpectableSubqueryable<ResultRow> =
        ReturningQueryable(with (BuilderContext) { buildStmt() }, references)

    override fun perform(ds: BlockingPerformer): Int = ds.statement(with (BuilderContext) { buildStmt() })
    override fun generateSql(ds: SqlPerformer): CompiledSql? = ds.generateSql(with (BuilderContext) { buildStmt() })
}