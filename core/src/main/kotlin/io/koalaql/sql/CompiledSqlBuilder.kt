package io.koalaql.sql

import io.koalaql.ddl.DataType
import io.koalaql.ddl.MappedDataType
import io.koalaql.ddl.TypeMapping
import io.koalaql.expr.Literal
import io.koalaql.identifier.Unquoted
import io.koalaql.identifier.Named
import io.koalaql.identifier.SqlIdentifier
import kotlin.reflect.KClass

class CompiledSqlBuilder(
    private val quoteStyle: IdentifierQuoteStyle
) {
    private val contents = StringBuilder()
    private val params = arrayListOf<Literal<*>>()
    private var errored = false

    private val mappings = hashMapOf<KClass<*>, MappedDataType<*, *>>()

    private val abridgements = arrayListOf<Abridgement>()

    private var abridgeFrom: Int = 0
    private var abridgeDepth: Int = 0

    fun beginAbridgement() {
        if (abridgeDepth++ == 0) abridgeFrom = contents.length
    }

    fun endAbridgement(summary: String) {
        if (--abridgeDepth == 0) abridgements.add(Abridgement(
            abridgeFrom,
            contents.length,
            summary
        ))
    }
    
    fun addSql(sql: String) {
        contents.append(sql)
    }

    fun addIdentifier(id: SqlIdentifier) {
        val exhaustive = when (id) {
            is Unquoted -> {
                addSql(id.id)
            }
            is Named -> {
                addSql(quoteStyle.quote)
                addSql(id.name)
                addSql(quoteStyle.quote)
            }
        }
    }

    fun addResolved(resolved: Resolved) {
        resolved.alias?.let {
            addIdentifier(it)
            addSql(".")
        }
        addIdentifier(resolved.innerName)
    }

    fun addError(error: String) {
        errored = true
        addSql("/* ERROR: $error */")
    }

    fun addMapping(type: DataType<*, *>) {
        if (type is MappedDataType) mappings.putIfAbsent(type.type, type)
    }

    fun addSql(sql: StandardSql) { addSql(sql.sql) }

    fun addLiteral(value: Literal<*>?) {
        if (value == null) {
            addSql("NULL")
        } else {
            contents.append("?")
            params.add(value)
        }
    }

    fun toSql(): CompiledSql {
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

    fun parenthesize(emitParens: Boolean = true, block: () -> Unit) {
        if (!emitParens) return block()

        addSql("(")
        block()
        addSql(")")
    }

    fun prefix(initial: String, after: String): SqlPrefix {
        var pref = initial

        return object : SqlPrefix {
            override fun next(block: () -> Unit) {
                addSql(pref)
                block()
                pref = after
            }
        }
    }
}