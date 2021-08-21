package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.sql.Scope

data class BuiltSelectQuery(
    val body: BuiltSelectBody = BuiltSelectBody(),

    val setOperations: MutableList<BuiltSetOperation> = arrayListOf(),

    var orderBy: List<Ordinal<*>> = emptyList(),

    var offset: Int = 0,
    var limit: Int? = null,

    var locking: LockMode? = null,

    var selected: List<Labeled<*>> = emptyList()
): BuiltQuery, BuiltStatement {
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