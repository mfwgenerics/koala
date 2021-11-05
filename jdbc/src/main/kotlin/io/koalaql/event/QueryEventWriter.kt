package io.koalaql.event

interface QueryEventWriter {
    /* iff successful, result contains rows affected or null for non-statements */
    fun finished(result: Result<Int?>)

    /* Called after `succeeded` once the results have been fully read. not guaranteed to be called */
    fun fullyRead(rows: Int)

    operator fun plus(other: QueryEventWriter): QueryEventWriter =
        CombinedQueryEventWriter(this, other)

    object Discard : QueryEventWriter {
        override fun finished(result: Result<Int?>) { }

        override fun fullyRead(rows: Int) { }

        override fun plus(other: QueryEventWriter): QueryEventWriter = other
    }
}