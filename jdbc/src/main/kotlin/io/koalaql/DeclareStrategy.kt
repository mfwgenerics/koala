package io.koalaql

sealed interface DeclareStrategy {
    companion object {
        val EXPECT = ReconcileTables(
            create = ReconcileMode.EXPECT,

            columns = ReconcileColumns(
                add = ReconcileMode.EXPECT,
                modify = ReconcileMode.EXPECT,
                drop = ReconcileMode.EXPECT
            ),

            indexes = ReconcileIndexes(
                add = ReconcileMode.EXPECT,
                drop = ReconcileMode.EXPECT
            ),

            drop = ReconcileMode.EXPECT
        )
    }
}
