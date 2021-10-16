package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.*
import io.koalaql.expr.Expr

interface ColumnDefaultable<T : Any>: ColumnKeyable<T> {
    private class Defaulted<T : Any>(
        val lhs: ColumnDefaultable<T>,
        val default: BuiltColumnDefault
    ): ColumnKeyable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
            default = this@Defaulted.default
            return lhs
        }
    }

    fun default(expr: Expr<T>): ColumnKeyable<T> =
        Defaulted(this, ColumnDefaultExpr(expr))

    fun default(value: T?): ColumnKeyable<T> =
        Defaulted(this, ColumnDefaultValue(value))
}
