package io.koalaql.sql

sealed class SqlResult<out T> {
    class Error(val message: String): SqlResult<Nothing>()
    class Value<T>(val value: T): SqlResult<T>()
}