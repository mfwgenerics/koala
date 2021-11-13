package io.koalaql.values

@Suppress("unchecked_cast")
fun <A : Any> RowSequence<RawResultRow>.unsafeCastToOneColumn() =
    this as RowSequence<RowOfOne<A>>

@Suppress("unchecked_cast")
fun <A : Any, B : Any> RowSequence<RawResultRow>.unsafeCastToTwoColumns() =
    this as RowSequence<RowOfTwo<A, B>>

@Suppress("unchecked_cast")
fun <A : Any, B : Any, C : Any> RowSequence<RawResultRow>.unsafeCastToThreeColumns() =
    this as RowSequence<RowOfThree<A, B, C>>