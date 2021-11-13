package io.koalaql.values

interface RowOfOne<A : Any>: ResultRow {
    fun firstOrNull(): A?

    fun first(): A = checkNotNull(firstOrNull())
        { "unexpected null in result. did you mean to use firstOrNull()?" }

    operator fun component1(): A = first()
}