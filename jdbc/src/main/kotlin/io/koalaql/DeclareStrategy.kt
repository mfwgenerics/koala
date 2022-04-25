package io.koalaql

sealed interface DeclareStrategy {
    object DoNothing: DeclareStrategy
    object CreateIfNotExists: DeclareStrategy

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

        val APPLY_ALL = ReconcileTables(
            create = ReconcileMode.APPLY,

            columns = ReconcileColumns(
                add = ReconcileMode.APPLY,
                modify = ReconcileMode.APPLY,
                drop = ReconcileMode.APPLY
            ),

            indexes = ReconcileIndexes(
                add = ReconcileMode.APPLY,
                drop = ReconcileMode.APPLY
            ),

            drop = ReconcileMode.APPLY
        )

        val NO_DROP = APPLY_ALL.copy(
            columns = APPLY_ALL.columns.copy(
                drop = ReconcileMode.EXPECT
            ),
            indexes = APPLY_ALL.indexes.copy(
                drop = ReconcileMode.EXPECT
            ),
            drop = ReconcileMode.EXPECT
        )

        val CREATE_ONLY = APPLY_ALL.copy(
            columns = APPLY_ALL.columns.copy(
                modify = ReconcileMode.EXPECT,
                drop = ReconcileMode.EXPECT
            ),
            indexes = APPLY_ALL.indexes.copy(
                drop = ReconcileMode.EXPECT
            ),
            drop = ReconcileMode.EXPECT
        )
    }
}
