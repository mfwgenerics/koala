package io.koalaql.query.fluent

import io.koalaql.query.WithOperand
import io.koalaql.query.WithType
import io.koalaql.query.built.BuiltWith

interface Withable<out R> {
    fun with(type: WithType, queries: List<BuiltWith>): R

    fun with(vararg queries: WithOperand): R =
        with(WithType.NOT_RECURSIVE, queries.map { it.buildWith() })

    fun withRecursive(vararg queries: WithOperand): R =
        with(WithType.RECURSIVE, queries.map { it.buildWith() })
}