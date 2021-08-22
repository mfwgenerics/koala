package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.ddl.DataType
import mfwgenerics.kotq.ddl.MappedColumnType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import java.sql.DatabaseMetaData
import java.sql.Types

class TableDiffer(
    val metadata: DatabaseMetaData
) {
    fun diffTable(
        table: Table,
        caseMode: CaseMode = CaseMode.UPPERCASE
    ): TableDiff {
        val tableName = caseMode.applyTo(table.relvarName.uppercase())

        val expectedColumnsByName = hashMapOf<String, BuiltColumnDef>()

        table.columns.associateByTo(
            expectedColumnsByName,
            { caseMode.applyTo(it.symbol) }
        ) {
            it.builtDef
        }

        val columns = metadata.getColumns(null, null, tableName, null)

        val result = TableDiff()

        while (columns.next()) {
            val name = columns.getString("COLUMN_NAME")

            val expected = expectedColumnsByName.remove(caseMode.applyTo(name))

            if (expected == null) {
                result.columns.dropped.add(name)
                continue
            }

            val dataType = when (val dt = columns.getInt("DATA_TYPE")) {
                Types.INTEGER -> DataType.INT32
                Types.SMALLINT -> DataType.INT16
                Types.VARCHAR -> DataType.VARCHAR(columns.getInt("COLUMN_SIZE"))
                else -> error("unrecognized SQL datatype $dt")
            }

            val expectedDataType = when (val dt = expected.columnType) {
                is MappedColumnType<*, *> -> dt.baseDataType
                else -> dt
            }

            val isAutoincrement = when (columns.getString("IS_AUTOINCREMENT")) {
                "YES" -> true
                else -> false
            }

            val diff = ColumnDefinitionDiff(
                type = if (!dataType.equivalentKind(expectedDataType)) {
                    /* TODO precision comparison */
                    expectedDataType
                } else null,
                notNull = when (columns.getString("IS_NULLABLE")) {
                    "YES" -> if (expected.notNull) expected.notNull else null
                    "NO" -> if (!expected.notNull) expected.notNull else null
                    else -> null
                },
                default = when (columns.getString("COLUMN_DEF")) {
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