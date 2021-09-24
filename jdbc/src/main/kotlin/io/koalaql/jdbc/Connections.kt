package io.koalaql.jdbc

import io.koalaql.KotqConnection
import io.koalaql.query.PerformableQuery
import io.koalaql.query.PerformableStatement

fun PerformableQuery.performWith(cxn: KotqConnection) =
    cxn.query(this)

fun PerformableStatement.performWith(cxn: KotqConnection) =
    cxn.statement(this)