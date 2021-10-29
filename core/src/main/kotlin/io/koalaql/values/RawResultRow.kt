package io.koalaql.values

@Suppress("unchecked_cast")
interface RawResultRow: ResultRow,
    RowWithThreeColumns<Any, Any, Any>
{
    operator fun get(ix: Int): Any?

    override fun firstOrNull(): Any? = get(0)
    override fun secondOrNull(): Any? = get(1)
    override fun thirdOrNull(): Any? = get(2)
}