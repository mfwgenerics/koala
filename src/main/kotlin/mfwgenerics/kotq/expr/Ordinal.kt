package mfwgenerics.kotq.expr

interface Ordinal<T : Any> {
    fun toOrderKey(): OrderKey<T>
}