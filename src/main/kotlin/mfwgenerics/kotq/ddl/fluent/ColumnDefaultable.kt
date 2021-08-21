package mfwgenerics.kotq.ddl.fluent

import mfwgenerics.kotq.ddl.built.*
import mfwgenerics.kotq.expr.Expr

interface ColumnDefaultable<T : Any>: ColumnReferenceable<T> {
    private class Defaulted<T : Any>(
        val lhs: ColumnDefaultable<T>,
        val default: BuiltColumnDefault
    ): ColumnReferenceable<T> {
        override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef {
            out.default = default
            return lhs
        }
    }

    fun default(expr: Expr<T>): ColumnReferenceable<T> =
        Defaulted(this, ColumnDefaultExpr(expr))

    fun default(value: T?): ColumnReferenceable<T> =
        Defaulted(this, ColumnDefaultValue(value))
}
