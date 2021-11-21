package io.koalaql.dialect

import io.koalaql.ddl.UnmappedDataType
import io.koalaql.expr.Reference
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.built.BuiltQuery
import io.koalaql.window.built.BuiltWindow

interface ExpressionCompiler {
    fun excluded(reference: Reference<*>)

    fun <T : Any> reference(emitParens: Boolean, value: Reference<T>)
    fun subquery(emitParens: Boolean, subquery: BuiltQuery)

    fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>)

    fun window(window: BuiltWindow)
}