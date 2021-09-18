package io.koalaql.event

interface QueryEventWriter {
    fun succeeded(rows: Int?)
    fun failed(ex: Exception)

    object Discard : QueryEventWriter {
        override fun succeeded(rows: Int?) { }
        override fun failed(ex: Exception) { }
    }
}