package io.koalaql.expr

import io.koalaql.query.BlockingPerformer
import io.koalaql.query.Distinctness
import io.koalaql.query.Queryable
import io.koalaql.query.SetOperationType
import io.koalaql.query.built.*
import io.koalaql.query.fluent.QueryableUnionOperand
import io.koalaql.values.RowSequence
import io.koalaql.values.RowWithOneColumn
import io.koalaql.values.unsafeCastToOneColumn

interface QueryableOfOne<T : Any>: Expr<T>, QueryableUnionOperand<RowWithOneColumn<T>> {
    class Wrap<T : Any>(
        private val queryable: QueryableUnionOperand<*>
    ): QueryableOfOne<T> {
        override fun BuiltFullQuery.buildIntoFullQuery(): FullQueryBuilder = queryable

        override fun BuiltFullQuery.buildIntoFullQueryTail(type: SetOperationType, distinctness: Distinctness) =
            with (queryable) { buildIntoFullQueryTail(type, distinctness) }
    }

    override fun performWith(ds: BlockingPerformer): RowSequence<RowWithOneColumn<T>> =
        ds.query(BuiltFullQuery.from(this)).unsafeCastToOneColumn()
}