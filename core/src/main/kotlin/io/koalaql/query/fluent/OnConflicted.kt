package io.koalaql.query.fluent

import io.koalaql.Assignment

interface OnConflicted: OnDuplicated {
    fun ignore(): GeneratingKeys

    override fun update(assignments: List<Assignment<*>>): GeneratingKeys
}