package io.koalaql.query

class AliasedCtedQueryable(
    private val asRelation: Aliased,
    private val asWithOperand: WithOperand
):
    WithOperand by asWithOperand,
    RelationBuilder by asRelation,
    GetsAliasedReference by asRelation