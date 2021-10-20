package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.*
import io.koalaql.expr.Expr

interface ColumnDefaultable<T : Any>: ColumnKeyable<T> {
    interface Nullable<T : Any>: ColumnKeyable.Nullable<T> {
        private class Defaulted<T : Any>(
            val lhs: Nullable<T>,
            val default: BuiltColumnDefault
        ): ColumnKeyable.Nullable<T> {
            override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
                default = this@Defaulted.default
                return lhs
            }
        }

        fun default(expr: Expr<T>): ColumnKeyable.Nullable<T> =
            Defaulted(this, ColumnDefaultExpr(expr))

        fun default(value: T?): ColumnKeyable.Nullable<T> =
            Defaulted(this, ColumnDefaultValue(value))
    }

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
