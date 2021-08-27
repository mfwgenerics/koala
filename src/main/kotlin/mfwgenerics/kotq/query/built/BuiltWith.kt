package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.Cte

class BuiltWith(
    val alias: Cte,
    val query: BuiltSubquery
)