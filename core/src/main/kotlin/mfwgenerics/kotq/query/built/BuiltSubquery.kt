package mfwgenerics.kotq.query.built

import mfwgenerics.kotq.expr.*
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
    val returning: List<Reference<*>> = emptyList()
): BuiltQuery {
    override fun populateScope(scope: Scope) {

    }
}

sealed interface BuiltSubquery: BuiltQuery, BuiltStatement {
    val columns: LabelList

    override fun populateScope(scope: Scope)
}

class BuiltQueryBody {
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

    fun populateScope(scope: Scope, standalone: Boolean) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }

        if (standalone) return

        relation.populateScope(scope)

        joins.forEach { join ->
            join.populateScope(scope)
        }
    }
}

class BuiltSelectQuery(
    val standalone: Boolean,
    val body: BuiltQueryBody,
    val selected: List<SelectedExpr<*>>
): BuiltSubquery, BuiltStatement {
    constructor(
        standalone: Boolean,
        body: BuiltQueryBody,
        references: List<SelectArgument>,
        includeAll: Boolean = false
    ): this(
        standalone,
        body,
        SelectionBuilder(body.withs.associateBy({ it.cte }) { it.query.columns })
            .also { builder ->
                if (includeAll) {
                    builder.fromRelation(body.relation)
                    body.joins.forEach { builder.fromRelation(it.to) }
                }

                references.forEach {
                    it.buildIntoSelection(builder)
                }
            }
            .toList()
    )

    override val columns: LabelList = LabelList(selected.map { it.name })

    override fun populateScope(scope: Scope) {
        body.populateScope(scope, standalone)

        selected.forEach {
            if (it.expr != it.name) scope.internal(it.name)
            scope.external(it.name)
        }
    }
}

data class BuiltValuesQuery(
    val values: RowSequence
): BuiltSubquery {
    override val columns: LabelList get() = values.columns

    override fun populateScope(scope: Scope) { }
}
