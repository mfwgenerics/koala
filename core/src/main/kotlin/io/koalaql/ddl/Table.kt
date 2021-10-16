package io.koalaql.ddl

import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.BuiltNamedIndex
import io.koalaql.ddl.fluent.ColumnDefinition
import io.koalaql.dsl.keys
import io.koalaql.expr.Expr
import io.koalaql.expr.RelvarColumn
import io.koalaql.query.Alias
import io.koalaql.query.Relvar
import io.koalaql.query.built.BuiltRelation

abstract class Table protected constructor(
    override val tableName: String
): Relvar {
    private val alias = Alias()

    override fun BuiltRelation.buildIntoRelation() {
        relation = this@Table
        setAliases(null, alias)
    }

    private val internalColumns = arrayListOf<TableColumn<*>>()
    override val columns: List<TableColumn<*>> get() = internalColumns

    private val usedNames: HashSet<String> = hashSetOf()

    private fun takeName(name: String) {
        check(usedNames.add(name)) { "field name $name is already in use" }
    }

    protected fun <T : Any> column(name: String, def: ColumnDefinition<T>): TableColumn<T> {
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

    protected fun <T : Any> column(name: String, def: DataType<*, T>): TableColumn<T> =
        column(name, BaseColumnType(def))

    var primaryKey: BuiltNamedIndex? = null
        private set

    private val internalIndexes = arrayListOf<BuiltNamedIndex>()
    val indexes: List<BuiltNamedIndex> get() = internalIndexes

    private fun nameKeys(keys: KeyList): String = keys.keys.asSequence()
        .map {
            when (it) {
                is RelvarColumn -> it.symbol
                else -> error("$it can not be named")
            }
        }
        .joinToString("_")

    private fun nameIndex(keys: KeyList, suffix: String): String =
        "${tableName}_${nameKeys(keys)}_$suffix"

    protected fun primaryKey(name: String, keys: KeyList): BuiltNamedIndex {
        check(primaryKey == null) { "multiple primary keys $name, $keys" }

        takeName(name)

        primaryKey = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.PRIMARY,
            keys = keys
        ))

        return primaryKey!!
    }

    protected fun uniqueKey(name: String, keys: KeyList): BuiltNamedIndex {
        takeName(name)

        val result = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.UNIQUE,
            keys = keys
        ))

        internalIndexes.add(result)

        return result
    }

    protected fun index(name: String, keys: KeyList): BuiltNamedIndex {
        takeName(name)

        val result = BuiltNamedIndex(name, BuiltIndexDef(
            type = IndexType.INDEX,
            keys = keys
        ))

        internalIndexes.add(result)

        return result
    }

    protected fun primaryKey(name: String, vararg keys: Expr<*>): BuiltNamedIndex =
        primaryKey(name, KeyList(keys.asList()))
    protected fun uniqueKey(name: String, vararg keys: Expr<*>): BuiltNamedIndex =
        uniqueKey(name, KeyList(keys.asList()))
    protected fun index(name: String, vararg keys: Expr<*>): BuiltNamedIndex =
        index(name, KeyList(keys.asList()))

    protected fun primaryKey(keys: KeyList): BuiltNamedIndex =
        primaryKey(nameIndex(keys, "pkey"), keys)
    protected fun uniqueKey(keys: KeyList): BuiltNamedIndex =
        uniqueKey(nameIndex(keys, "key"), keys)
    protected fun index(keys: KeyList): BuiltNamedIndex =
        index(nameIndex(keys, "idx"), keys)

    protected fun primaryKey(vararg keys: Expr<*>): BuiltNamedIndex =
        primaryKey(KeyList(keys.asList()))
    protected fun uniqueKey(vararg keys: Expr<*>): BuiltNamedIndex =
        uniqueKey(KeyList(keys.asList()))
    protected fun index(vararg keys: Expr<*>): BuiltNamedIndex =
        index(KeyList(keys.asList()))

    override fun toString(): String = tableName

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