package io.koalaql.expr

import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithOneColumn
import io.koalaql.values.unsafeCastToOneColumn

interface ExprQueryableUnionOperand<T : Any>: ExprQueryable<T>, QueryableUnionOperand<RowWithOneColumn<T>> {
    override fun perform(ds: BlockingPerformer): RowSequence<RowWithOneColumn<T>> =
        ds.query(BuiltQuery.from(this)).unsafeCastToOneColumn()

    override fun with(type: WithType, queries: List<BuiltWith>) = object : Queryable<RowWithOneColumn<T>> {
        override fun perform(ds: BlockingPerformer): RowSequence<RowWithOneColumn<T>> =
            ds.query(BuiltQuery.from(this)).unsafeCastToOneColumn()

        override fun BuiltQuery.buildInto(): QueryBuilder {
            withType = type
            withs = queries

            return this@ExprQueryableUnionOperand
        }
    }
}