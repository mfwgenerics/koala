package io.koalaql.dialect

import io.koalaql.ddl.UnmappedDataType
import io.koalaql.expr.Reference
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.built.BuiltFullQuery
import io.koalaql.sql.SqlTextBuilder
import io.koalaql.window.built.BuiltWindow

interface ExpressionCompiler {
    val sql: SqlTextBuilder

    fun excluded(reference: Reference<*>)

    fun <T : Any> reference(emitParens: Boolean, value: Reference<T>)
    fun subquery(emitParens: Boolean, subquery: BuiltFullQuery)

    fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>)

    fun window(window: BuiltWindow)
}