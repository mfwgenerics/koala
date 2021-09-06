package mfwgenerics.kotq.jdbc

import mfwgenerics.kotq.data.INTEGER
import mfwgenerics.kotq.data.SMALLINT
import mfwgenerics.kotq.data.VARCHAR
import mfwgenerics.kotq.ddl.BaseColumnType
import mfwgenerics.kotq.ddl.IndexType
import mfwgenerics.kotq.ddl.Table
import mfwgenerics.kotq.ddl.TableColumn
import mfwgenerics.kotq.ddl.diff.ChangedDefault
import mfwgenerics.kotq.ddl.diff.ColumnDiff
import mfwgenerics.kotq.ddl.diff.TableDiff
import java.sql.DatabaseMetaData
import java.sql.Types

class TableDiffer(
    val dbName: String,
    val metadata: DatabaseMetaData
) {
    private fun diffColumns(table: Table, result: TableDiff) {
        val tableName = table.relvarName

        val expectedColumnsByName = hashMapOf<String, TableColumn<*>>()

        table.columns.associateByTo(expectedColumnsByName) { it.symbol }

        val columns = metadata.getColumns(dbName, null, tableName, null)

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

            val diff = ColumnDiff(
                newColumn = expected,
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
                result.columns.altered[name] = diff
            }
        }

        expectedColumnsByName.forEach { (name, def) ->
            result.columns.created[name] = def
        }
    }

    private data class IndexPart(
        val unique: Boolean,
        val ordinal: Int,
        val columnName: String
    )

    private data class PrimaryKeyPart(
        val name: String,
        val ordinal: Int,
        val columnName: String
    )

    private data class IndexWithKeyNames(
        val name: String,
        val type: IndexType,
        val columns: List<String>
    ) {
        fun equivalent(other: IndexWithKeyNames): Boolean {
            if (type != other.type) return false
            if (columns.size != other.columns.size) return false

            columns.forEachIndexed { ix, col ->
                if (col != other.columns[ix]) return false
            }

            return true
        }
    }

    private fun fetchExistingPrimaryKey(tableName: String): IndexWithKeyNames? {
        val pkResults = metadata.getPrimaryKeys(dbName, null, tableName)

        val pkParts = arrayListOf<PrimaryKeyPart>()

        while (pkResults.next()) {
            pkParts.add(PrimaryKeyPart(
                name = pkResults.getString("PK_NAME"),
                ordinal = pkResults.getInt("KEY_SEQ"),
                columnName = pkResults.getString("COLUMN_NAME")
            ))
        }

        return pkParts
            .takeIf { it.isNotEmpty() }
            ?.let { parts ->
                parts.sortBy { it.ordinal }

                IndexWithKeyNames(
                    name = parts.first().name,
                    type = IndexType.PRIMARY,
                    columns = parts.map { it.columnName }
                )
            }
    }

    private fun diffKeys(table: Table, result: TableDiff) {
        val tableName = table.relvarName

        val existingPrimaryKey = fetchExistingPrimaryKey(tableName)

        val indexResults = metadata.getIndexInfo(dbName, null, tableName, false, false)

        val indexInfosByName = hashMapOf<String, ArrayList<IndexPart>>()

        while (indexResults.next()) {
            val name = indexResults.getString("INDEX_NAME")

            if (name == null || name == existingPrimaryKey?.name) continue

            val infos = indexInfosByName.getOrPut(name) { arrayListOf() }

            infos.add(IndexPart(
                unique = !indexResults.getBoolean("NON_UNIQUE"),
                ordinal = indexResults.getInt("ORDINAL_POSITION"),
                columnName = indexResults.getString("COLUMN_NAME")
            ))
        }

        val expectedIndexesByName = table.indexes.associateByTo(hashMapOf(), { it.name })

        indexInfosByName.forEach { (name, parts) ->
            parts.sortBy { it.ordinal }

            val existing = IndexWithKeyNames(
                name = name,
                type = if (parts.first().unique) {
                    IndexType.UNIQUE
                } else {
                    IndexType.INDEX
                },
                columns = parts.map { it.columnName }
            )

            val expected = expectedIndexesByName.remove(name)

            if (expected == null) {
                result.indexes.dropped.add(name)
            } else {
                val converted = IndexWithKeyNames(
                    name = expected.name,
                    type = expected.def.type,
                    columns = expected.def.keys.keys.map { expr ->
                        when (expr) {
                            is TableColumn -> expr.symbol
                            else -> error("not implemented")
                        }
                    }
                )

                if (!existing.equivalent(converted)) {
                    result.indexes.altered[name] = expected.def
                }
            }
        }

        expectedIndexesByName.forEach { (name, it) ->
            result.indexes.created[name] = it.def
        }
    }

    fun diffTable(
        table: Table
    ): TableDiff {
        val result = TableDiff(table)

        diffColumns(table, result)
        diffKeys(table, result)

        return result
    }
}