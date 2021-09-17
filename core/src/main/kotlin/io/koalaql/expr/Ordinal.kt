package io.koalaql.expr

interface Ordinal<T : Any> {
    fun toOrderKey(): OrderKey<T>
}