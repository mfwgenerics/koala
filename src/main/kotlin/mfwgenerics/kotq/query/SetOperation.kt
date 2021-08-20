package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuildsIntoSelect
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSetOperation
import mfwgenerics.kotq.query.fluent.UnionOperand
import mfwgenerics.kotq.query.fluent.Unionable

class SetOperation(
    val of: Unionable,
    val against: UnionOperand,
    val type: SetOperationType,
    val distinctness: Distinctness
): Unionable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.setOperations.add(BuiltSetOperation(
            type = type,
            distinctness = distinctness,
            body = against.buildSelect()
        ))

        return of
    }
}