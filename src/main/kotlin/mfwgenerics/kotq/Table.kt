package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.Named

abstract class Table(
    val name: String
): Relation {
    fun <T : Any> column(name: String, type: ColumnType<T>): Named<T> {
        return object : Named<T> {
            override val name: Name<T> = Name()
        }
    }
}