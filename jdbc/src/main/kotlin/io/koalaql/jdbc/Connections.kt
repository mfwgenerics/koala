package io.koalaql.jdbc

import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement

fun PerformableQuery.performWith(cxn: JdbcConnection) =
    cxn.query(this)

fun PerformableStatement.performWith(cxn: JdbcConnection) =
    cxn.statement(this)