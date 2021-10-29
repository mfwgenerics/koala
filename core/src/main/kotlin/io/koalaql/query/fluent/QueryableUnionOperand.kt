package io.koalaql.query.fluent

import io.koalaql.query.Queryable
import io.koalaql.values.ResultRow

interface QueryableUnionOperand: Queryable<ResultRow>, UnionOperand