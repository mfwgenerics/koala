package io.koalaql.sql

data class Resolved(
    val alias: String? = null,
    val innerName: String
)