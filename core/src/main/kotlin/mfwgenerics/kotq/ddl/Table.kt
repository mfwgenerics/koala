package mfwgenerics.kotq.ddl

import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.ddl.built.BuiltIndexDef
import mfwgenerics.kotq.ddl.built.BuiltNamedIndex
import mfwgenerics.kotq.ddl.fluent.ColumnDefinition
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

        return TableColumn(this, name, def).also { internalColumns.add(it) }
    }

    fun <T : Any> column(name: String, def: DataType<T>): TableColumn<T> {
        takeName(name)

        return TableColumn(this, name, BaseColumnType(def)).also { internalColumns.add(it) }
    }

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

    fun primaryKey(name: String, keys: KeyList) {
        check(primaryKey == null) { "duplicate primary key $name" }

        takeName(name)

        primaryKey = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.PRIMARY,
            keys = keys
        ))
    }

    fun primaryKey(keys: KeyList) =
        primaryKey("${relvarName}_${nameKeys(keys)}_pkey", keys)

    fun uniqueIndex(name: String, keys: KeyList) {
        takeName(name)

        internalIndexes.add(BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.UNIQUE,
            keys = keys
        )))
    }

    fun index(name: String, keys: KeyList) {
        takeName(name)

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