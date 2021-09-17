package io.koalaql.query

import io.koalaql.query.built.BuildsIntoQueryBody
import io.koalaql.query.built.BuiltQueryBody
import io.koalaql.query.built.BuiltSetOperation
import io.koalaql.query.fluent.UnionOperand
import io.koalaql.query.fluent.Unionable

class SetOperation(
    val of: Unionable,
    val against: UnionOperand,
    val type: SetOperationType,
    val distinctness: Distinctness
): Unionable {
    override fun buildIntoQueryBody(out: BuiltQueryBody): BuildsIntoQueryBody? {
        out.setOperations.add(BuiltSetOperation(
            type = type,
            distinctness = distinctness,
            body = against.buildUnionOperand()
        ))

        return of
    }
}