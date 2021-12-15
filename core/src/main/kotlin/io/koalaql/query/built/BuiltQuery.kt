package io.koalaql.query.built

import io.koalaql.expr.Ordinal
import io.koalaql.expr.Reference
import io.koalaql.query.BackwardsList
import io.koalaql.query.WithType
import io.koalaql.sql.Scope

class BuiltQuery: BuiltSubquery, BuiltWithable {
    override val columns: List<Reference<*>> get() = head.columns

    override var withType = WithType.NOT_RECURSIVE
    override var withs: List<BuiltWith> = emptyList()

    lateinit var head: BuiltUnionOperandQuery

    val unioned = BackwardsList<BuiltSetOperation>()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    override fun columnsUnnamed(): Boolean = head.columnsUnnamed()

    override fun populateScope(scope: Scope) {
        if (columnsUnnamed()) {
            columns.forEachIndexed { ix, it -> scope.unnamed(it, ix) }
        } else {
            columns.forEach { scope.internal(it) }
        }
    }

    fun populateCtes(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }
    }

    fun finishBuild(expectedColumnOrder: List<Reference<*>>?) {
        head.computeColumns(withs)
        unioned.forEach { it.body.computeColumns(withs) }

        val reorderRequired = unioned.isNotEmpty() || expectedColumnOrder != null

        if (!reorderRequired) return

        val allReferences = linkedSetOf<Reference<*>>()

        allReferences.addAll(head.columns)
        unioned.forEach { allReferences.addAll(it.body.columns) }

        val reorderTo = expectedColumnOrder
            ?.let { expected ->
                check (allReferences.size == expected.size && allReferences.containsAll(expected)) {
                    "$expected did not match $allReferences"
                }

                expected
            }
            ?: allReferences

        head.changeColumnsToFit(reorderTo)
        unioned.forEach { it.body.changeColumnsToFit(reorderTo) }
    }
}