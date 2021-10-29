package io.koalaql.query.fluent

import io.koalaql.expr.QueryableOfTwo

interface QueryableOfTwoUnionOperand<A : Any, B : Any>: QueryableOfTwo<A, B>, UnionOperand