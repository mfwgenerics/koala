package io.koalaql.query.built

import io.koalaql.expr.Ordinal
import io.koalaql.expr.Reference
import io.koalaql.query.ReversedList
import io.koalaql.query.WithType
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltQuery: BuiltDml, BuiltQueryable, BuiltWithable {
    val columns: List<Reference<*>> get() = head.columns

    override var withType = WithType.NOT_RECURSIVE
    override var withs: List<BuiltWith> = emptyList()

    lateinit var head: BuiltUnionOperandQuery

    val unioned = ReversedList<BuiltSetOperation>()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    fun columnsUnnamed(): Boolean = head.columnsUnnamed()

    override fun populateScope(scope: Scope) {
        head.columns.forEach { scope.internal(it) }
    }

    fun populateCtes(scope: Scope) {
        withs.forEach {
            scope.cte(it.cte, it.query.columns)
        }
    }

    fun finishBuild() {
        head.computeColumns(withs)
        unioned.forEach { it.body.computeColumns(withs) }

        if (unioned.isEmpty()) return

        val allReferences = linkedSetOf<Reference<*>>()

        allReferences.addAll(head.columns)
        unioned.forEach { allReferences.addAll(it.body.columns) }

        head.changeColumnsToFit(allReferences)
        unioned.forEach { it.body.changeColumnsToFit(allReferences) }
    }

    companion object {
        fun from(builder: QueryBuilder): BuiltQuery =
            unfoldBuilder(builder, BuiltQuery()) { it.buildInto() }
                .apply { finishBuild() }
    }
}