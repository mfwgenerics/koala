package io.koalaql.query.fluent

import io.koalaql.query.QueryableResults
import io.koalaql.values.ResultRow

interface QueryableResultsUnionOperand: QueryableUnionOperand<ResultRow>, QueryableResults