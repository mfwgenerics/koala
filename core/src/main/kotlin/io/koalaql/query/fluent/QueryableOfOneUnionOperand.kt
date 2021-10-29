package io.koalaql.query.fluent

import io.koalaql.expr.QueryableOfOne

interface QueryableOfOneUnionOperand<T : Any>: QueryableOfOne<T>, UnionOperand