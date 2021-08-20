package mfwgenerics.kotq

import mfwgenerics.kotq.dsl.Relvar
import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.NamedReference
import mfwgenerics.kotq.expr.RelvarColumn
import kotlin.reflect.KClass

abstract class Table(
    override val name: String
): Relvar {
    class Column<T : Any>(
        type: KClass<T>,
        symbol: String,
        val name: Name<T>,
        val columnType: ColumnType<T>
    ): RelvarColumn<T>(symbol, type, name.identifier)

    private val internalColumns = arrayListOf<Column<*>>()

    override val columns: List<Column<*>> get() = internalColumns

    fun <T : Any> column(name: String, type: ColumnType<T>): NamedReference<T> =
        Column(type.type, name, Name(type.type, IdentifierName()), type).also { internalColumns.add(it) }

    override fun namedExprs(): List<Labeled<*>> = columns.flatMap {
        it.namedExprs()
    }
}