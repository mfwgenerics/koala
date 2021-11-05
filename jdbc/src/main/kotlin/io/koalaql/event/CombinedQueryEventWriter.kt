package io.koalaql.event

class CombinedQueryEventWriter(
    private val lhs: QueryEventWriter,
    private val rhs: QueryEventWriter
): QueryEventWriter {
    override fun finished(result: Result<Int?>) {
        lhs.finished(result)
        rhs.finished(result)
    }

    override fun fullyRead(rows: Int) {
        lhs.fullyRead(rows)
        rhs.fullyRead(rows)
    }
}