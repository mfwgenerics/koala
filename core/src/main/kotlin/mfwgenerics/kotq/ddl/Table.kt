package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.ddl.built.BuiltIndexDef
import mfwgenerics.kotq.ddl.built.BuiltNamedIndex
import mfwgenerics.kotq.ddl.fluent.ColumnDefinition
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.query.Relvar

open class Table(
    override val relvarName: String
): Relvar {
    private val internalColumns = arrayListOf<TableColumn<*>>()
    override val columns: List<TableColumn<*>> get() = internalColumns

    private val usedIndexNames: HashSet<String> = hashSetOf()
    private val usedColumnNames: HashSet<String> = hashSetOf()

    private fun takeIndexName(name: String) {
        check(usedIndexNames.add(name)) { "index name $name is already in use" }
    }

    private fun takeFieldName(name: String) {
        check(usedColumnNames.add(name)) { "field name $name is already in use" }
    }

    fun <T : Any> column(name: String, def: ColumnDefinition<T>): TableColumn<T> {
        takeFieldName(name)

        return TableColumn(this, name, def).also { internalColumns.add(it) }
    }

    fun <T : Any> column(name: String, def: DataType<T>): TableColumn<T> {
        takeFieldName(name)

        return TableColumn(this, name, BaseColumnType(def)).also { internalColumns.add(it) }
    }

    var primaryKey: BuiltNamedIndex? = null
        private set

    val internalIndexes = arrayListOf<BuiltNamedIndex>()
    val indexes: List<BuiltNamedIndex> get() = internalIndexes

    fun primaryKey(name: String, keys: KeyList) {
        check(primaryKey == null) { "duplicate primary key $name" }

        takeIndexName(name)

        primaryKey = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.PRIMARY,
            keys = keys
        ))
    }

    fun uniqueIndex(name: String, keys: KeyList) {
        takeIndexName(name)

        internalIndexes.add(BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.UNIQUE,
            keys = keys
        )))
    }

    fun index(name: String, keys: KeyList) {
        takeIndexName(name)

        internalIndexes.add(BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.INDEX,
            keys = keys
        )))
    }

    companion object {
        fun <T : Any> DataType<T>.autoIncrement() = BaseColumnType(this).autoIncrement()

        fun <T : Any> DataType<T>.nullable() = BaseColumnType(this).nullable()

        fun <T : Any> DataType<T>.default(expr: Expr<T>) = BaseColumnType(this).default(expr)
        fun <T : Any> DataType<T>.default(value: T?) = BaseColumnType(this).default(value)

        fun <T : Any> DataType<T>.reference(column: TableColumn<T>) = BaseColumnType(this).reference(column)
    }
}