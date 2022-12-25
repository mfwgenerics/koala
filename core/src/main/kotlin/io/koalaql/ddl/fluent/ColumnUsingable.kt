package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.*
import io.koalaql.expr.Expr

interface ColumnUsingable<T : Any>: ColumnKeyable<T> {
    interface Nullable<T : Any>: ColumnKeyable.Nullable<T> {
        private class Usinged<T : Any>(
            val lhs: Nullable<T>,
            val using: (Expr<*>) -> Expr<*>
        ): ColumnKeyable.Nullable<T> {
            override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
                using = this@Usinged.using
                return lhs
            }
        }

        fun using(using: (Expr<*>) -> Expr<T>): ColumnKeyable.Nullable<T> =
            Usinged(this, using)
    }

    private class Usinged<T : Any>(
        val lhs: ColumnUsingable<T>,
        val using: (Expr<*>) -> Expr<*>
    ): ColumnKeyable<T> {
        override fun BuiltColumnDef.buildIntoColumnDef(): ColumnDefBuilder {
            using = this@Usinged.using
            return lhs
        }
    }

    fun using(using: (Expr<*>) -> Expr<T>): ColumnKeyable<T> =
        Usinged(this, using)
}
