package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.SelectedExpr
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.expr.Reference
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
    val returning: List<SelectedExpr<*>> = emptyList()
): BuiltQuery {
    override fun populateScope(scope: Scope) {

    }
}

sealed interface BuiltSubquery: BuiltQuery, BuiltStatement {
    val columns: LabelList

    fun exports(): Sequence<Reference<*>>

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

    var selected: List<SelectedExpr<*>> = emptyList()

    override lateinit var columns: LabelList

    override fun exports(): Sequence<Reference<*>> =
        selected.asSequence().flatMap { it.namedExprs() }.map { it.name }

    override fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { it.populateScope(scope) }

        withs.forEach {
            // TODO
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

    override fun exports(): Sequence<Reference<*>> =
        columns.values.asSequence()

    override fun populateScope(scope: Scope) { }
}
