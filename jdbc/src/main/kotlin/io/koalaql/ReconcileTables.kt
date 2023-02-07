package io.koalaql

import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.ddl.diff.TableDiff

data class ReconcileTables(
    val create: ReconcileMode,

    val columns: ReconcileColumns,
    val indexes: ReconcileIndexes,

    val drop: ReconcileMode
): DeclareStrategy {
    fun filterChanges(changes: SchemaChange): ReconciledChanges {
        val result = ReconciledChanges()

        result
            .of(create)
            .tables
            .created
            .putAll(changes.tables.created)

        val addIndexesDest = result
            .of(indexes.add)
            .tables
            .altered

        val dropIndexesDest = result
            .of(indexes.drop)
            .tables
            .altered

        val addColumnsDest = result
            .of(columns.add)
            .tables
            .altered

        val modifyColumnsDest = result
            .of(columns.modify)
            .tables
            .altered

        val dropColumnsDest = result
            .of(columns.drop)
            .tables
            .altered

        changes
            .tables
            .altered
            .forEach { (name, diff) ->
                diff.indexes.created.forEach { (iname, def) ->
                    addIndexesDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .indexes
                        .created[iname] = def
                }

                diff.indexes.altered.forEach { (iname, def) ->
                    dropIndexesDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .indexes
                        .created[iname] = def
                }

                diff.indexes.dropped.forEach { iname ->
                    dropIndexesDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .indexes
                        .dropped
                        .add(iname)
                }

                diff.columns.created.forEach { (cname, column) ->
                    addColumnsDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .columns
                        .created[cname] = column
                }

                diff.columns.altered.forEach { (cname, column) ->
                    modifyColumnsDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .columns
                        .altered[cname] = column
                }

                diff.columns.dropped.forEach { dropped ->
                    dropColumnsDest.getOrPut(name) { TableDiff(diff.newTable) }
                        .columns
                        .dropped
                        .add(dropped)
                }
            }

        result
            .of(drop)
            .tables
            .dropped
            .addAll(changes.tables.dropped)

        return result
    }
}