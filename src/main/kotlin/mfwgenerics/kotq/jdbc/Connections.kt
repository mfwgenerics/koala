package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.dsl.Queryable
import mfwgenerics.kotq.values.RowSequence

fun Queryable.performWith(cxn: ConnectionWithDialect): RowSequence =
    cxn.query(this)