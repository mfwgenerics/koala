package io.koalaql.query

import io.koalaql.Assignment
import io.koalaql.ddl.built.BuiltNamedIndex

sealed interface OnConflictOrDuplicateAction

sealed interface OnConflictAction: OnConflictOrDuplicateAction {
    val key: OnConflictKey
}

class OnConflictIgnore(
    override val key: OnConflictKey
): OnConflictAction

class OnConflictUpdate(
    override val key: OnConflictKey,
    val assignments: List<Assignment<*>>
): OnConflictAction

class OnDuplicateUpdate(
    val assignments: List<Assignment<*>>
): OnConflictOrDuplicateAction