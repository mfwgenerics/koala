package io.koalaql.sql

import io.koalaql.expr.Literal

class SqlTextBuilder(
    private val quoteStyle: IdentifierQuoteStyle
) {
    private val contents = StringBuilder()
    private val params = arrayListOf<Literal<*>>()
    private var errored = false

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

    fun addIdentifier(id: String) {
        addSql(quoteStyle.quote)
        addSql(id)
        addSql(quoteStyle.quote)
    }

    fun addResolved(resolved: Resolved) {
        resolved.alias?.let {
            addSql("$it.")
        }
        addIdentifier(resolved.innerName)
    }

    fun addError(error: String) {
        errored = true
        addSql("/* ERROR: $error */")
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

    fun toSql(): SqlText {
        if (errored) throw GeneratedSqlException("Unable to generate SQL. See incomplete SQL below:\n$contents")

        return SqlText(
            abridgements,
            "$contents",
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