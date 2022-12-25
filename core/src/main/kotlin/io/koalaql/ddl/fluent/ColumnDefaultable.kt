package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.*
import io.koalaql.expr.Expr

interface ColumnDefaultable<T : Any>: ColumnUsingable<T> {
    interface Nullable<T : Any>: ColumnUsingable.Nullable<T> {
        private class Defaulted<T : Any>(
            val lhs: Nullable<T>,
            val default: BuiltColumnDefault
        ): ColumnUsingable.Nullable<T> {
            override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
                default = this@Defaulted.default
                return lhs
            }
        }

        fun default(expr: Expr<T>): ColumnUsingable.Nullable<T> =
            Defaulted(this, ColumnDefaultExpr(expr))

        fun default(value: T?): ColumnUsingable.Nullable<T> =
            Defaulted(this, ColumnDefaultValue(value))
    }

    private class Defaulted<T : Any>(
        val lhs: ColumnDefaultable<T>,
        val default: BuiltColumnDefault
    ): ColumnUsingable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
            default = this@Defaulted.default
            return lhs
        }
    }

    fun default(expr: Expr<T>): ColumnUsingable<T> =
        Defaulted(this, ColumnDefaultExpr(expr))

    fun default(value: T?): ColumnUsingable<T> =
        Defaulted(this, ColumnDefaultValue(value))
}
