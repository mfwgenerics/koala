package io.koalaql

import io.koalaql.ddl.diff.SchemaChange

class ReconciledChanges(
    val applied: SchemaChange = SchemaChange(),
    val unexpected: SchemaChange = SchemaChange(),
    val ignored: SchemaChange = SchemaChange()
) {
    fun of(mode: ReconcileMode): SchemaChange = when (mode) {
        ReconcileMode.APPLY -> applied
        ReconcileMode.EXPECT -> unexpected
        ReconcileMode.IGNORE -> ignored
    }
}