package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.ddl.built.BuiltColumnDefault

data class ChangedDefault(
    val default: BuiltColumnDefault?
)