package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.Cte

class BuiltWith(
    val cte: Cte,
    val query: BuiltSubquery
)