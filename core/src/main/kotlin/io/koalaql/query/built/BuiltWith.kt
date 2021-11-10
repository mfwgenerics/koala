package io.koalaql.query.built

import io.koalaql.query.Cte

class BuiltWith(
    val cte: Cte,
    val query: BuiltFullQuery
)