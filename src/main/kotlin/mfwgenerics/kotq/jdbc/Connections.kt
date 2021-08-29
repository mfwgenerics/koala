package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.query.Queryable
import mfwgenerics.kotq.query.Updated
import mfwgenerics.kotq.query.fluent.Inserted
import mfwgenerics.kotq.values.RowSequence

fun Queryable.performWith(cxn: ConnectionWithDialect): RowSequence =
    cxn.query(this)

fun Inserted.performWith(cxn: ConnectionWithDialect) {
    cxn.execute(this)
}

fun Updated.performWith(cxn: ConnectionWithDialect) {
    cxn.execute(this)
}