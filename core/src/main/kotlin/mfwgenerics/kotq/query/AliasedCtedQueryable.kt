package mfwgenerics.kotq.query

class AliasedCtedQueryable(
    private val asRelation: Aliased,
    private val asWithOperand: WithOperand
):
    WithOperand by asWithOperand,
    AliasedRelation by asRelation,
    GetsAliasedReference by asRelation