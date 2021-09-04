package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.SMALLINT
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.BaseColumnType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.diff.ChangedDefault
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import java.sql.DatabaseMetaData
import java.sql.Types

class TableDiffer(
    val dbName: String,
    val metadata: DatabaseMetaData
) {
    fun diffTable(
        table: Table
    ): TableDiff {
        val tableName = table.relvarName

        val expectedColumnsByName = hashMapOf<String, BuiltColumnDef>()

        table.columns.associateByTo(
            expectedColumnsByName,
            { it.symbol }
        ) {
            it.builtDef
        }

        val columns = metadata.getColumns(dbName, null, tableName, null)

        val result = TableDiff()

        while (columns.next()) {
            val name = columns.getString("COLUMN_NAME")

            val expected = expectedColumnsByName.remove(name)

            if (expected == null) {
                result.columns.dropped.add(name)
                continue
            }

            val dataType = when (val dt = columns.getInt("DATA_TYPE")) {
                Types.INTEGER -> INTEGER
                Types.SMALLINT -> SMALLINT
                Types.VARCHAR -> VARCHAR(columns.getInt("COLUMN_SIZE"))
                else -> error("unrecognized SQL datatype $dt")
            }

            val expectedDataType = expected.columnType.dataType

            val isAutoincrement = when (columns.getString("IS_AUTOINCREMENT")) {
                "YES" -> true
                else -> false
            }

            val diff = ColumnDefinitionDiff(
                type = if (dataType != expectedDataType) {
                    /* TODO precision comparison */
                    BaseColumnType(expectedDataType)
                } else null,
                notNull = when (columns.getString("IS_NULLABLE")) {
                    "YES" -> if (expected.notNull) expected.notNull else null
                    "NO" -> if (!expected.notNull) expected.notNull else null
                    else -> null
                },
                changedDefault = when (columns.getString("COLUMN_DEF")) {
                    null -> if (expected.default != null) {
                        ChangedDefault(expected.default)
                    } else null
                    else -> if (expected.default == null && !isAutoincrement) {
                        ChangedDefault(expected.default)
                    } else null
                },
                isAutoIncrement = null
            )

            if (!diff.doesNothing()) {
                result.columns.altered[name] = diff
            }
        }

        expectedColumnsByName.forEach { (name, def) ->
            result.columns.created[name] = def
        }

        return result
    }
}