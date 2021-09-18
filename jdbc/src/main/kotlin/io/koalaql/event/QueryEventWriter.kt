package io.koalaql.event

interface QueryEventWriter {
    fun succeeded(rows: Int?)
    fun failed(ex: Exception)

    /* Called after `succeeded` once the results have been read. not guaranteed to be called */
    fun finished(rows: Int)

    object Discard : QueryEventWriter {
        override fun succeeded(rows: Int?) { }
        override fun failed(ex: Exception) { }

        override fun finished(rows: Int) { }
    }
}