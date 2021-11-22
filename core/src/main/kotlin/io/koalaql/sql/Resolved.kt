package io.koalaql.sql

import io.koalaql.identifier.SqlIdentifier

data class Resolved(
    val alias: SqlIdentifier? = null,
    val innerName: SqlIdentifier
)