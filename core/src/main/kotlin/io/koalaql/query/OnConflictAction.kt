package io.koalaql.query

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex

sealed interface OnConflictOrDuplicateAction

sealed interface OnConflictAction: OnConflictOrDuplicateAction {
    val key: BuiltNamedIndex
}

class OnConflictIgnore(
    override val key: BuiltNamedIndex
): OnConflictAction

class OnConflictUpdate(
    override val key: BuiltNamedIndex,
    val assignments: List<Assignment<*>>
): OnConflictAction

class OnDuplicateUpdate(
    val assignments: List<Assignment<*>>
): OnConflictOrDuplicateAction