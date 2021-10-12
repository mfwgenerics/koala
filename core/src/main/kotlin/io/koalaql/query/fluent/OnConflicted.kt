package io.koalaql.query.fluent

import io.koalaql.Assignment

interface OnConflicted: OnDuplicated {
    fun ignore(): Returningable

    override fun update(assignments: List<Assignment<*>>): Returningable
}