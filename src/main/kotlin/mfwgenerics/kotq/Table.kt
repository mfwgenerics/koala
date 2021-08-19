package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.Named
import kotlin.reflect.KClass

abstract class Table(
    val name: String
): Relation {
    class Column<T : Any>(
        type: KClass<T>,
        val name: Name<T>,
        val symbol: String,
        val columnType: ColumnType<T>
    ): Named<T>(type, name.identifier)

    private val internalColumns = arrayListOf<Column<*>>()

    val columns: List<Column<*>> get() = internalColumns

    fun <T : Any> column(name: String, type: ColumnType<T>): Named<T> =
        Column(type.type, Name(type.type, IdentifierName()), name, type).also { internalColumns.add(it) }

    override fun namedExprs(): List<Labeled<*>> = columns.flatMap {
        it.namedExprs()
    }
}