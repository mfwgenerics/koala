package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.Reference

class AliasedCtedQueryable(
    private val asRelation: Aliased,
    private val asWithOperand: WithOperand
):
    WithOperand by asWithOperand,
    AliasedRelation by asRelation
{
    operator fun <T : Any> get(reference: Reference<T>) = asRelation[reference]
}