package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.dsl.LabelList
import mfwgenerics.kotq.sql.Scope

interface BuiltQuery: BuiltStatement {
    val columns: LabelList

    fun populateScope(scope: Scope)
}