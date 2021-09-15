package mfwgenerics.kotq.dialect

import mfwgenerics.kotq.data.UnmappedDataType
import mfwgenerics.kotq.expr.Reference
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.sql.SqlTextBuilder
import mfwgenerics.kotq.window.built.BuiltWindow

interface ExpressionCompiler {
    val sql: SqlTextBuilder

    fun excluded(reference: Reference<*>)

    fun <T : Any> reference(emitParens: Boolean, value: Reference<T>)
    fun subquery(emitParens: Boolean, subquery: BuiltSubquery)

    fun aggregatable(emitParens: Boolean, aggregatable: BuiltAggregatable)
    fun <T : Any> dataTypeForCast(to: UnmappedDataType<T>)

    fun window(window: BuiltWindow)
}