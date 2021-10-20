package io.koalaql.jdbc

import io.koalaql.DataConnection
import io.koalaql.DataSource
import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement
import io.koalaql.transact

fun PerformableQuery.performWith(cxn: DataConnection) =
    cxn.query(this)

fun PerformableStatement.performWith(cxn: DataConnection) =
    cxn.statement(this)

fun PerformableQuery.performWith(
    db: DataSource
) = db.transact {
    this.performWith(it).toList().asSequence()
}

fun PerformableStatement.performWith(
    db: DataSource
) = db.transact {
    this.performWith(it)
}