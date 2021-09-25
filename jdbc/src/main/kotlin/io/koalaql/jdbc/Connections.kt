package io.koalaql.jdbc

import io.koalaql.DataConnection
import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement

fun PerformableQuery.performWith(cxn: DataConnection) =
    cxn.query(this)

fun PerformableStatement.performWith(cxn: DataConnection) =
    cxn.statement(this)