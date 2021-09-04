package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.SMALLINT
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.BaseColumnType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.diff.ChangedDefault
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import mfwgenerics.kotq.ddl.diff.Alteration
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

        val expectedColumnsByName = hashMapOf<String, TableColumn<*>>()

        table.columns.associateByTo(expectedColumnsByName) { it.symbol }

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

            val def = expected.builtDef

            val expectedDataType = def.columnType.dataType

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
                    "YES" -> if (def.notNull) def.notNull else null
                    "NO" -> if (!def.notNull) def.notNull else null
                    else -> null
                },
                changedDefault = when (columns.getString("COLUMN_DEF")) {
                    null -> if (def.default != null) {
                        ChangedDefault(def.default)
                    } else null
                    else -> if (def.default == null && !isAutoincrement) {
                        ChangedDefault(def.default)
                    } else null
                },
                isAutoIncrement = null
            )

            if (!diff.doesNothing()) {
                result.columns.altered[name] = Alteration(expected, diff)
            }
        }

        expectedColumnsByName.forEach { (name, def) ->
            result.columns.created[name] = def
        }

        return result
    }
}