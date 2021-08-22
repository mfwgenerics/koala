package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.values.RowSequence

sealed interface BuiltQuery

class BuiltReturningInsert(
    val insert: BuiltInsert,
    val returning: List<Labeled<*>> = emptyList()
): BuiltQuery

sealed interface BuiltSubquery: BuiltQuery, BuiltStatement {
    val columns: LabelList

    fun populateScope(scope: Scope)
}

data class BuiltSelectQuery(
    val body: BuiltSelectBody = BuiltSelectBody(),

    val setOperations: MutableList<BuiltSetOperation> = arrayListOf(),

    var orderBy: List<Ordinal<*>> = emptyList(),

    var offset: Int = 0,
    var limit: Int? = null,

    var locking: LockMode? = null,

    var selected: List<Labeled<*>> = emptyList()
): BuiltSubquery, BuiltStatement {
    override lateinit var columns: LabelList

    override fun populateScope(scope: Scope) {
        body.populateScope(scope)

        selected.forEach {
            it.namedExprs().forEach { labeled ->
                if (labeled.expr != labeled.name) scope.internal(labeled.name)
                scope.external(labeled.name)
            }
        }
    }
}

data class BuiltValuesQuery(
    val values: RowSequence
): BuiltSubquery {
    override val columns: LabelList get() = values.columns

    override fun populateScope(scope: Scope) {
        columns.values.forEach { scope.external(it) }
    }
}
