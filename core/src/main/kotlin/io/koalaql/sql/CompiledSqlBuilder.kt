package io.koalaql.sql

import io.koalaql.ddl.DataType
import io.koalaql.ddl.MappedDataType
import io.koalaql.expr.Literal
import io.koalaql.identifier.Named
import io.koalaql.identifier.SqlIdentifier
import io.koalaql.identifier.Unquoted
import io.koalaql.sql.token.*
import kotlin.reflect.KClass

class CompiledSqlBuilder(
    private val escapes: SqlEscapes
) {
    private val tokens = arrayListOf<SqlToken>()

    private val mappings = hashMapOf<KClass<*>, MappedDataType<*, *>>()

    fun beginAbridgement() {
        tokens.add(BeginAbridgement)
    }

    fun endAbridgement(summary: String) {
        tokens.add(EndAbridgement(summary))
    }
    
    fun addSql(sql: String) {
        tokens.add(RawSqlToken(sql))
    }

    fun addIdentifier(id: SqlIdentifier) {
        tokens.add(IdentifierToken(id))
    }

    fun addResolved(resolved: Resolved) {
        resolved.alias?.let {
            addIdentifier(it)
            addSql(".")
        }
        addIdentifier(resolved.innerName)
    }

    fun addError(error: String) {
        tokens.add(ErrorToken(error))
    }

    fun addMapping(type: DataType<*, *>) {
        if (type is MappedDataType) mappings.putIfAbsent(type.type, type)
    }

    fun addLiteral(value: Literal<*>?) {
        if (value == null) {
            addSql("NULL")
        } else {
            tokens.add(LiteralToken(value))
        }
    }

    fun toSql(): CompiledSql {
        val contents = StringBuilder()
        val params = arrayListOf<Literal<*>>()
        var errored = false

        val abridgements = arrayListOf<Abridgement>()

        var abridgeFrom = 0
        var abridgeDepth = 0

        tokens.forEach { token ->
            when (token) {
                BeginAbridgement -> {
                    if (abridgeDepth++ == 0) abridgeFrom = contents.length
                }
                is EndAbridgement -> {
                    if (--abridgeDepth == 0) abridgements.add(Abridgement(
                        abridgeFrom,
                        contents.length,
                        token.summary
                    ))
                }
                is ErrorToken -> {
                    errored = true
                    contents.append("/* ERROR: ${token.message} */")
                }
                is IdentifierToken -> when (val id = token.identifier) {
                    is Unquoted -> {
                        contents.append(id.id)
                    }
                    is Named -> escapes.identifier(contents, id)
                }
                is LiteralToken -> {
                    val value = token.value
                    val mappedValue = mappings[value.type]
                        ?.unconvertLiteralUnchecked(value)
                        ?: value

                    escapes.literal(contents, params, mappedValue)
                }
                is RawSqlToken -> {
                    contents.append(token.sql)
                }
            }
        }

        if (errored) throw GeneratedSqlException("Unable to generate SQL. See incomplete SQL below:\n$contents")

        return CompiledSql(
            abridgements,
            "$contents",
            TypeMappings(mappings),
            params
        )
    }

    inline fun <T> withResult(result: SqlResult<T>, block: (T) -> Unit) = when (result) {
        is SqlResult.Error -> addError(result.message)
        is SqlResult.Value -> block(result.value)
    }
}