package io.koalaql.values

interface RowOfThree<A : Any, B : Any, C : Any>: RowOfTwo<A, B> {
    fun thirdOrNull(): C?

    fun third(): C = checkNotNull(thirdOrNull())
        { "unexpected null in result. did you mean to use thirdOrNull()?" }

    operator fun component3(): C = third()
}