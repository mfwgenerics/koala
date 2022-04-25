package io.koalaql.sql

import io.koalaql.expr.Literal

data class CompiledSql(
    private val abridgements: List<Abridgement>,
    val parameterizedSql: String,

    val mappings: TypeMappings,
    val parameters: List<Literal<*>>
) {
    fun toAbridgedSql(): String {
        val builder = StringBuilder()

        var from = 0

        abridgements.forEach {
            builder.append(parameterizedSql, from, it.from)

            builder.append(it.summary)

            from = it.toExclusive
        }

        builder.append(parameterizedSql, from, parameterizedSql.length)

        return "$builder"
    }

    override fun toString(): String = "$parameterizedSql\n${parameters.joinToString(", ")}"
}