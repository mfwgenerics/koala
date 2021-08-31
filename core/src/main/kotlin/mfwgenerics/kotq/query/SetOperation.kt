package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuildsIntoQueryBody
import mfwgenerics.kotq.query.built.BuiltQueryBody
import mfwgenerics.kotq.query.built.BuiltSetOperation
import mfwgenerics.kotq.query.fluent.UnionOperand
import mfwgenerics.kotq.query.fluent.Unionable

class SetOperation(
    val of: Unionable,
    val against: UnionOperand,
    val type: SetOperationType,
    val distinctness: Distinctness
): Unionable {
    override fun buildIntoSelect(out: BuiltQueryBody): BuildsIntoQueryBody? {
        out.setOperations.add(BuiltSetOperation(
            type = type,
            distinctness = distinctness,
            body = against.buildUnionOperand()
        ))

        return of
    }
}