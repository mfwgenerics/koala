package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.ddl.DataType
import mfwgenerics.kotq.ddl.MappedColumnType
import mfwgenerics.kotq.ddl.Table
import java.sql.DatabaseMetaData
import java.sql.Types

class TableDiffer(
    val metadata: DatabaseMetaData
) {
    fun diffTable(
        table: Table,
        caseMode: CaseMode = CaseMode.UPPERCASE
    ) {
        val tableName = caseMode.applyTo(table.relvarName.uppercase())

        val expectedColumnsByName = table.columns.associateBy({
            caseMode.applyTo(it.symbol)
        }) {
            it.builtDef
        }

        val columns = metadata.getColumns(null, null, tableName, null)

        while (columns.next()) {
            val name = columns.getString("COLUMN_NAME")

            val expected = expectedColumnsByName[caseMode.applyTo(name)]

            if (expected != null) {
                val dataType = when (val dt = columns.getInt("DATA_TYPE")) {
                    Types.INTEGER -> DataType.INT32
                    Types.SMALLINT -> DataType.INT16
                    else -> error("unrecognized SQL datatype $dt")
                }

                val expectedDataType = when (val dt = expected.columnType) {
                    is MappedColumnType<*, *> -> dt.dataType
                    else -> dt
                }

                println(name)
                println(ColumnDefinitionDiff(
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
                        else -> if (expected.default == null) {
                            ChangedDefault(expected.default)
                        } else null
                    },
                    isAutoIncrement = null
                ))
            }
        }
    }
}