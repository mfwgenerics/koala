package mfwgenerics.kotq.ddl.fluent

import mfwgenerics.kotq.ddl.built.*
import mfwgenerics.kotq.expr.Expr

interface ColumnDefaultable<T : Any>: ColumnKeyable<T> {
    private class Defaulted<T : Any>(
        val lhs: ColumnDefaultable<T>,
        val default: BuiltColumnDefault
    ): ColumnKeyable<T> {
        override fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef {
            out.default = default
            return lhs
        }
    }

    fun default(expr: Expr<T>): ColumnKeyable<T> =
        Defaulted(this, ColumnDefaultExpr(expr))

    fun default(value: T?): ColumnKeyable<T> =
        Defaulted(this, ColumnDefaultValue(value))
}
