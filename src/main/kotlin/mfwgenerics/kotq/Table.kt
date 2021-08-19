package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.Named
import mfwgenerics.kotq.expr.LabeledExpr
import kotlin.reflect.KClass

abstract class Table(
    val name: String
): Relation {
    class Column<T : Any>(
        override val name: Name<T>,
        val symbol: String,
        val columnType: ColumnType<T>
    ): Named<T>() {
        override val type: KClass<T> get() = name.type
    }

    private val internalColumns = arrayListOf<Column<*>>()

    val columns: List<Column<*>> get() = internalColumns

    fun <T : Any> column(name: String, type: ColumnType<T>): Named<T> =
        Column(Name(type.type, IdentifierName(name)), name, type).also { internalColumns.add(it) }

    override fun namedExprs(): List<Labeled<*>> = columns.flatMap {
        it.namedExprs()
    }
}