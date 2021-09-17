package io.koalaql.jdbc

import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement

fun PerformableQuery.performWith(cxn: JdbcConnection) =
    cxn.perform(this)

fun PerformableStatement.performWith(cxn: JdbcConnection) =
    cxn.perform(this)