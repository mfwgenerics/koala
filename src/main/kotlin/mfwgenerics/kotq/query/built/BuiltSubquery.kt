package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Labeled
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.LockMode
import mfwgenerics.kotq.query.WithType
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.values.RowSequence
import mfwgenerics.kotq.window.LabeledWindow

sealed interface BuiltQuery {
    fun populateScope(scope: Scope)
}

class BuiltReturningInsert(
    val insert: BuiltInsert,
    val returning: List<Labeled<*>> = emptyList()
): BuiltQuery {
    override fun populateScope(scope: Scope) {

    }
}

sealed interface BuiltSubquery: BuiltQuery, BuiltStatement {
    val columns: LabelList

    override fun populateScope(scope: Scope)
}

class BuiltSelectQuery: BuiltSubquery, BuiltStatement {
    lateinit var relation: BuiltRelation

    var withType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    val joins: MutableList<BuiltJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    var groupBy: List<Expr<*>> = arrayListOf()
    var having: Expr<Boolean>? = null

    var windows: List<LabeledWindow> = emptyList()

    val setOperations: MutableList<BuiltSetOperation> = arrayListOf()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    var locking: LockMode? = null

    var selected: List<Labeled<*>> = emptyList()

    override lateinit var columns: LabelList

    override fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { it.populateScope(scope) }

        withs.forEach {
            val innerScope = scope.innerScope()

            it.query.populateScope(innerScope)

            innerScope.externals().forEach { name ->
                scope.internal(it.alias[name], name, it.alias)
            }

            scope.register(it.alias, innerScope)
        }

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
