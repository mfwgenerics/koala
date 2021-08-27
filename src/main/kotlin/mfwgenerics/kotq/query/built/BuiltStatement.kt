package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.sql.Scope

sealed interface BuiltStatement {
    fun populateScope(scope: Scope)
}