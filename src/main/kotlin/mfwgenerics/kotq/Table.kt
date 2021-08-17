package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.Named
import mfwgenerics.kotq.expr.LabeledExpr

abstract class Table(
    val name: String
): Relation {
    class Column<T : Any>(
        override val name: Name<T>,
        val symbol: String,
        val type: ColumnType<T>
    ): Named<T>

    private val internalColumns = arrayListOf<Column<*>>()

    val columns: List<Column<*>> get() = internalColumns

    fun <T : Any> column(name: String, type: ColumnType<T>): Named<T> =
        Column(Name(type.type, name), name, type).also { internalColumns.add(it) }

    override fun namedExprs(): List<Labeled<*>> = columns.flatMap {
        it.namedExprs()
    }
}