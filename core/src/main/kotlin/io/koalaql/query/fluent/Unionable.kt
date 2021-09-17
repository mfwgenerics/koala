package io.koalaql.query.fluent

import io.koalaql.query.Distinctness
import io.koalaql.query.SetOperation
import io.koalaql.query.SetOperationType

interface Unionable: Orderable {
    fun union(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.DISTINCT)
    fun unionAll(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.ALL)

    fun intersect(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.DISTINCT)
    fun intersectAll(against: UnionableUnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.ALL)

    fun except(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.DISTINCT)
    fun exceptAll(against: UnionableUnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.ALL)
}