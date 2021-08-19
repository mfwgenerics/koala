package mfwgenerics.kotq.sql

class SqlTextBuilder {
    private val contents = StringBuilder()
    private val params = arrayListOf<Any?>()

    fun addSql(sql: String) {
        contents.append(sql)
    }

    fun addValue(value: Any?) {
        contents.append("?")
        params.add(value)
    }

    fun toSql(): SqlText = SqlText(
        "$contents",
        params
    )

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