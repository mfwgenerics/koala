package mfwgenerics.kotq

abstract class Table(
    val name: String
): Aliasable {
    fun <T : Any> column(name: String, type: ColumnType<T>): Column<T> {
        return object : Column<T>() { }
    }
}