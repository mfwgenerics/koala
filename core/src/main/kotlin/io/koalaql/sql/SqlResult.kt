package io.koalaql.sql

sealed class SqlResult<out T> {
    data class Error(val message: String): SqlResult<Nothing>()
    data class Value<T>(val value: T): SqlResult<T>()
}