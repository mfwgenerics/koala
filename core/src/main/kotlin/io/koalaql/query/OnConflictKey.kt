package io.koalaql.query

import io.koalaql.ddl.TableColumn
import io.koalaql.ddl.built.BuiltNamedIndex

sealed interface OnConflictKey

data class OnConflictKeyIndex(
    val index: BuiltNamedIndex
): OnConflictKey

data class OnConflictKeyColumns(
    val columns: List<TableColumn<*>>
): OnConflictKey