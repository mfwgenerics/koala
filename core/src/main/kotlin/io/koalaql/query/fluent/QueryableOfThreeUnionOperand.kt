package io.koalaql.query.fluent

import io.koalaql.expr.QueryableOfThree

interface QueryableOfThreeUnionOperand<A : Any, B : Any, C : Any>: QueryableOfThree<A, B, C>, UnionOperand