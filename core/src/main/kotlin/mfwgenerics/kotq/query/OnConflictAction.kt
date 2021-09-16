package mfwgenerics.kotq.query

import mfwgenerics.kotq.Assignment
import mfwgenerics.kotq.ddl.built.BuiltNamedIndex

sealed class OnConflictAction private constructor() {
    abstract val keys: List<BuiltNamedIndex>

    class Ignore(
        override val keys: List<BuiltNamedIndex>
    ): OnConflictAction()

    data class Update(
        override val keys: List<BuiltNamedIndex>,
        val assignments: List<Assignment<*>>
    ): OnConflictAction()
}
