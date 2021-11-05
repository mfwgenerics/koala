package io.koalaql

data class ReconcileColumns(
    val add: ReconcileMode,
    val modify: ReconcileMode,
    val drop: ReconcileMode
)