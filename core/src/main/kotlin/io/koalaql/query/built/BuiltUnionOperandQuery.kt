package io.koalaql.query.built

import io.koalaql.dsl.null_
import io.koalaql.expr.*
import io.koalaql.query.Distinctness
import io.koalaql.query.LabelListOf
import io.koalaql.query.Values
import io.koalaql.sql.Scope
import io.koalaql.values.ReshapedValuesRowIterator

sealed interface BuiltQueryable: PopulatesScope

class BuiltGeneratesKeysInsert(
    val insert: BuiltInsert,
    val returning: Column<*>
): BuiltQueryable {
    override fun populateScope(scope: Scope) {

    }
}

sealed interface BuiltUnionOperandQuery: PopulatesScope {
    val columns: List<Reference<*>>

    fun columnsUnnamed(): Boolean

    fun computeColumns(withs: List<BuiltWith>)

    fun changeColumnsToFit(references: Iterable<Reference<*>>)
}

class BuiltSelectQuery(
    val body: BuiltQueryBody,
    val selectArgs: List<SelectArgument>,
    val includeAll: Boolean,
    var distinctness: Distinctness
): BuiltUnionOperandQuery {
    lateinit var selected: List<SelectedExpr<*>>

    override lateinit var columns: List<Reference<*>>

    override fun columnsUnnamed(): Boolean = false

    override fun computeColumns(withs: List<BuiltWith>) {
        selected = SelectionBuilder(withs.associateBy({ it.cte }) { it.query.columns })
            .also { builder ->
                if (includeAll) {
                    builder.fromRelation(body.relation)
                    body.joins.forEach { builder.fromRelation(it.to) }
                }

                selectArgs.forEach {
                    with (it) {
                        builder.buildIntoSelection()
                    }
                }
            }
            .toList()

        columns = selected.map { it.name }
    }

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
    var values: Values
): BuiltUnionOperandQuery {
    override val columns get() = values.columns

    override fun computeColumns(withs: List<BuiltWith>) { }

    override fun populateScope(scope: Scope) { }

    override fun changeColumnsToFit(references: Iterable<Reference<*>>) {
        val labelList = LabelListOf(references.toList())

        val oldValues = values

        values = Values(labelList) {
            ReshapedValuesRowIterator(labelList, oldValues.valuesIterator())
        }
    }

    override fun columnsUnnamed(): Boolean = true
}
