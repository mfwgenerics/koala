package mfwgenerics.kotq.ddl.diff

import mfwgenerics.kotq.ddl.built.BuiltColumnDefault

data class ChangedDefault(
    val default: BuiltColumnDefault?
)