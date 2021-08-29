package mfwgenerics.kotq.sql

import mfwgenerics.kotq.expr.Literal

class SqlTextBuilder {
    private val contents = StringBuilder()
    private val params = arrayListOf<Literal<*>>()
    
    fun addSql(sql: String) {
        contents.append(sql)
    }

    fun addSql(sql: StandardSql) { addSql(sql.sql) }

    fun addValue(value: Literal<*>?) {
        if (value == null) {
            addSql("NULL")
        } else {
            contents.append("?")
            params.add(value)
        }
    }

    fun toSql(): SqlText = SqlText(
        "$contents",
        params
    )

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