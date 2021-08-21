package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.ddl.IndexType
import mfwgenerics.kotq.ddl.KeyList

class BuiltIndexDef(
    val type: IndexType,
    val keys: KeyList
)