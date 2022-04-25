package io.koalaql

import io.koalaql.sql.CompiledSql

class ReconciledDdl(
    val applied: List<CompiledSql>,
    val unexpected: List<CompiledSql>,
    val ignored: List<CompiledSql>
)