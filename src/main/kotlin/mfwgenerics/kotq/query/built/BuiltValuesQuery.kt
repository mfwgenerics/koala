package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.values.RowSequence

data class BuiltValuesQuery(
    val values: RowSequence
): BuiltQuery {
    override val columns: LabelList get() = values.columns

    override fun populateScope(scope: Scope) {
        columns.values.forEach { scope.external(it) }
    }
}