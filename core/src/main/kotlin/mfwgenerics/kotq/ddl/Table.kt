package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.UnmappedDataType
import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.ddl.Table.Companion.reference
import mfwgenerics.kotq.ddl.built.BuiltIndexDef
import mfwgenerics.kotq.ddl.built.BuiltNamedIndex
import mfwgenerics.kotq.ddl.fluent.ColumnDefinition
import mfwgenerics.kotq.dsl.keys
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.RelvarColumn
import mfwgenerics.kotq.query.Relvar

open class Table(
    override val relvarName: String
): Relvar {
    private val internalColumns = arrayListOf<TableColumn<*>>()
    override val columns: List<TableColumn<*>> get() = internalColumns

    private val usedNames: HashSet<String> = hashSetOf()

    private fun takeName(name: String) {
        check(usedNames.add(name)) { "field name $name is already in use" }
    }

    fun <T : Any> column(name: String, def: ColumnDefinition<T>): TableColumn<T> {
        takeName(name)

        val column = TableColumn(this, name, def)

        column.builtDef.markedAsKey?.let {
            when (it) {
                IndexType.PRIMARY -> primaryKey(keys(column))
                IndexType.UNIQUE -> uniqueKey(keys(column))
                IndexType.INDEX -> index(keys(column))
            }
        }

        internalColumns.add(column)

        return column
    }

    fun <T : Any> column(name: String, def: DataType<*, T>): TableColumn<T> =
        column(name, BaseColumnType(def))

    var primaryKey: BuiltNamedIndex? = null
        private set

    val internalIndexes = arrayListOf<BuiltNamedIndex>()
    val indexes: List<BuiltNamedIndex> get() = internalIndexes

    fun nameKeys(keys: KeyList): String = keys.keys.asSequence()
        .map {
            when (it) {
                is RelvarColumn -> it.symbol
                else -> error("$it can not be named")
            }
        }
        .joinToString("_")

    fun nameIndex(keys: KeyList, suffix: String): String =
        "${relvarName}_${nameKeys(keys)}_$suffix"

    fun primaryKey(name: String, keys: KeyList): BuiltNamedIndex {
        check(primaryKey == null) { "multiple primary keys $name, $keys" }

        takeName(name)

        primaryKey = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.PRIMARY,
            keys = keys
        ))

        return primaryKey!!
    }

    fun uniqueKey(name: String, keys: KeyList): BuiltNamedIndex {
        takeName(name)

        val result = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.UNIQUE,
            keys = keys
        ))

        internalIndexes.add(result)

        return result
    }

    fun index(name: String, keys: KeyList): BuiltNamedIndex {
        takeName(name)

        val result = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.INDEX,
            keys = keys
        ))

        internalIndexes.add(result)

        return result
    }

    fun primaryKey(keys: KeyList) = primaryKey(nameIndex(keys, "pkey"), keys)
    fun uniqueKey(keys: KeyList) = uniqueKey(nameIndex(keys, "key"), keys)
    fun index(keys: KeyList) = index(nameIndex(keys, "idx"), keys)

    companion object {
        fun <T : Any> DataType<*, T>.autoIncrement() = BaseColumnType(this).autoIncrement()

        fun <T : Any> DataType<*, T>.nullable() = BaseColumnType(this).nullable()

        fun <T : Any> DataType<*, T>.default(expr: Expr<T>) = BaseColumnType(this).default(expr)
        fun <T : Any> DataType<*, T>.default(value: T?) = BaseColumnType(this).default(value)

        fun <T : Any> DataType<*, T>.reference(column: TableColumn<T>) = BaseColumnType(this).reference(column)

        fun <T : Any> DataType<*, T>.primaryKey() = BaseColumnType(this).primaryKey()
        fun <T : Any> DataType<*, T>.uniqueKey() = BaseColumnType(this).uniqueKey()
    }
}