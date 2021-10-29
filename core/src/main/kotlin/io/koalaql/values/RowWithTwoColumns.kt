package io.koalaql.values

interface RowWithTwoColumns<A : Any, B : Any>: RowWithOneColumn<A> {
    fun secondOrNull(): B?

    fun second(): B = checkNotNull(secondOrNull())
        { "unexpected null in result. did you mean to use secondOrNull()?" }

    operator fun component2(): B = second()
}