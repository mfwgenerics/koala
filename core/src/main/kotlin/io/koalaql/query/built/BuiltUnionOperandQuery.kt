package io.koalaql.query.built

import io.koalaql.dsl.null_
import io.koalaql.expr.*
import io.koalaql.query.Values
import io.koalaql.sql.Scope

sealed interface BuiltQuery: PopulatesScope

class BuiltGeneratesKeysInsert(
    val insert: BuiltInsert,
    val returning: Column<*>
): BuiltQuery {
    override fun populateScope(scope: Scope) {

    }
}


sealed interface BuiltUnionOperandQuery: PopulatesScope {
    val columns: List<Reference<*>>

    fun columnsUnnamed(): Boolean

    fun changeColumnsToFit(references: Iterable<Reference<*>>)
}

class BuiltSelectQuery private constructor(
    val body: BuiltQueryBody,
    var selected: List<SelectedExpr<*>>
): BuiltUnionOperandQuery {
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

    override var columns: List<Reference<*>> = selected.map { it.name }

    override fun columnsUnnamed(): Boolean = false

    override fun populateScope(scope: Scope) {
        body.populateScope(scope)

        selected.forEach {
            if (it.expr != it.name) scope.internal(it.name)
            scope.external(it.name)
        }
    }

    override fun changeColumnsToFit(references: Iterable<Reference<*>>) {
        val selectedByReference = selected.associateBy { it.name }

        columns = references.toList()

        selected = references.map {
            @Suppress("unchecked_cast")
            selectedByReference[it] ?: SelectedExpr(null_(), it as Reference<Any>)
        }
    }
}

data class BuiltValuesQuery(
    val values: Values
): BuiltUnionOperandQuery {
    override val columns get() = values.columns

    override fun populateScope(scope: Scope) { }

    override fun changeColumnsToFit(references: Iterable<Reference<*>>) { error("not implemented") }

    override fun columnsUnnamed(): Boolean = true
}
