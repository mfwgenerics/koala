package io.koalaql

data class ReconcileIndexes(
    val add: ReconcileMode,
    val drop: ReconcileMode
)