package io.koalaql.sql

import io.koalaql.expr.Literal

class SqlTextBuilder(
    private val quoteStyle: IdentifierQuoteStyle
) {
    private val contents = StringBuilder()
    private val params = arrayListOf<Literal<*>>()
    private var errored = false
    
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
            "$contents",
            params
        )
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