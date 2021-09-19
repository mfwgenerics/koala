package io.koalaql.query.built

import io.koalaql.expr.*
import io.koalaql.query.LabelList
import io.koalaql.query.LabelListOf
import io.koalaql.query.LockMode
import io.koalaql.query.WithType
import io.koalaql.sql.Scope
import io.koalaql.values.RowSequence
import io.koalaql.window.LabeledWindow
import kotlin.reflect.KClass

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

    fun populateScope(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }

        relation.populateScope(scope)

        joins.forEach { join ->
            join.populateScope(scope)
        }
    }
}

class BuiltSelectQuery(
    val body: BuiltQueryBody,
    val selected: List<SelectedExpr<*>>
): BuiltSubquery, BuiltStatement {
    constructor(
        body: BuiltQueryBody,
        references: List<SelectArgument>,
        includeAll: Boolean = false
    ): this(
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

    fun reorderToMatchUnion(outerSelected: List<SelectedExpr<*>>): BuiltSelectQuery {
        val selectedByNames = selected.associateByTo(hashMapOf()) { it.name }

        check (selectedByNames.size == selected.size) {
            "duplicate labels in select"
        }

        val selected = outerSelected.map {
            val corresponding = selectedByNames.remove(it.name)

            @Suppress("unchecked_cast")
            corresponding ?: (SelectedExpr(Literal(it.name.type as KClass<Any>, null), it.name as Reference<Any>))
        }

        check (selectedByNames.isEmpty()) {
            "Labels ${selectedByNames.keys} appeared in union selected but not in top level select"
        }

        return BuiltSelectQuery(
            body = body,
            selected = selected
        )
    }

    override val columns: LabelList = LabelListOf(selected.map { it.name })

    override fun populateScope(scope: Scope) {
        body.populateScope(scope)

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
