package io.koalaql.ddl.built

import io.koalaql.ddl.IndexType
import io.koalaql.ddl.KeyList

class BuiltIndexDef(
    val type: IndexType,
    val keys: KeyList
)