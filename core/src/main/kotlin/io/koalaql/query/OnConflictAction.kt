package io.koalaql.query

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex

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
