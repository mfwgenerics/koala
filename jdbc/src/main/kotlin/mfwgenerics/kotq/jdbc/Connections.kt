package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.query.PerformableQuery
import mfwgenerics.kotq.query.PerformableStatement

fun PerformableQuery.performWith(cxn: JdbcConnection) =
    cxn.perform(this)

fun PerformableStatement.performWith(cxn: JdbcConnection) =
    cxn.perform(this)