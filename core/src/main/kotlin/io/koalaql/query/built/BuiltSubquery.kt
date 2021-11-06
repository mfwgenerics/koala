package io.koalaql.query.built

import io.koalaql.dsl.null_
import io.koalaql.expr.*
import io.koalaql.query.LabelList
import io.koalaql.query.LabelListOf
import io.koalaql.query.Values
import io.koalaql.sql.Scope
import kotlin.reflect.KClass

sealed interface BuiltQuery {
    fun populateScope(scope: Scope)
}

class BuiltGeneratesKeysInsert(
    val insert: BuiltInsert,
    val returning: Column<*>
): BuiltQuery {
    override fun populateScope(scope: Scope) {

    }
}

sealed interface BuiltSubquery: BuiltQuery, BuiltDml {
    val columns: List<Reference<*>>

    override fun populateScope(scope: Scope)
}

class BuiltSelectQuery(
    val body: BuiltQueryBody,
    val selected: List<SelectedExpr<*>>
): BuiltSubquery, BuiltDml {
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
                    with (it) {
                        builder.buildIntoSelection()
                    }
                }
            }
            .toList()
    )

    fun reorderToMatchUnion(outerSelected: List<SelectedExpr<*>>): BuiltSelectQuery {
        val selectedByNames = selected.associateByTo(hashMapOf()) { it.name }

        check(selectedByNames.size == selected.size) {
            "duplicate labels in select"
        }

        val selected = outerSelected.map {
            val corresponding = selectedByNames.remove(it.name)

            @Suppress("unchecked_cast")
            corresponding ?: (SelectedExpr(null_(), it.name as Reference<Any>))
        }

        check(selectedByNames.isEmpty()) {
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
    val values: Values
): BuiltSubquery {
    override val columns get() = values.columns

    override fun populateScope(scope: Scope) { }
}
