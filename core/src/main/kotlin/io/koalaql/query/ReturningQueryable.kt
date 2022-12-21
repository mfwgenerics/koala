package io.koalaql.query

import io.koalaql.expr.Reference
import io.koalaql.expr.SelectOperand
import io.koalaql.expr.SelectionBuilder
import io.koalaql.query.built.BuilderContext
import io.koalaql.query.built.BuiltReturning
import io.koalaql.query.built.BuiltStatement
import io.koalaql.query.built.BuiltSubquery
import io.koalaql.values.ResultRow
import io.koalaql.values.RowSequence

class ReturningQueryable(
    private val stmt: BuiltStatement,
    private val returned: List<SelectOperand<*>>
): ExpectableSubqueryable<ResultRow> {
    override fun BuilderContext.buildQuery(expectedColumns: List<Reference<*>>?): BuiltSubquery {
        val builder = SelectionBuilder(emptyMap())

        returned.forEach {
            with (it) { builder.buildIntoSelection() }
        }

        return BuiltReturning(stmt, builder.toList())
    }

    override fun perform(ds: BlockingPerformer): RowSequence<ResultRow> {
        error("not implemented")
    }
}