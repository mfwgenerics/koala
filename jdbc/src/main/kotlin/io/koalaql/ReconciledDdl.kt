package io.koalaql

import io.koalaql.sql.SqlText

class ReconciledDdl(
    val applied: List<SqlText>,
    val unexpected: List<SqlText>,
    val ignored: List<SqlText>
)