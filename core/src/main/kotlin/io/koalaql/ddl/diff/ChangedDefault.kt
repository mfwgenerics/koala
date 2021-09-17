package io.koalaql.ddl.diff

import io.koalaql.ddl.built.BuiltColumnDefault

data class ChangedDefault(
    val default: BuiltColumnDefault?
)