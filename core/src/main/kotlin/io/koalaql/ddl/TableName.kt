package io.koalaql.ddl

data class TableName(
    val schema: String? = null,
    val name: String
) {
    override fun toString(): String =
        schema?.let { "$it.$name" }?:name
}