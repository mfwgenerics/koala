package mfwgenerics.kotq.query

import mfwgenerics.kotq.Assignment

sealed class OnConflictAction private constructor() {
    object Ignore: OnConflictAction()

    data class Update(
        val assignments: List<Assignment<*>>
    ): OnConflictAction()
}
