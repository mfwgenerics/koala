package io.koalaql.query.built

import io.koalaql.expr.Ordinal
import io.koalaql.expr.Reference
import io.koalaql.query.ReversedList
import io.koalaql.sql.Scope
import io.koalaql.unfoldBuilder

class BuiltFullQuery: BuiltDml, BuiltQuery {
    val columns: List<Reference<*>> get() = head.columns

    lateinit var head: BuiltUnionOperandQuery

    val unioned = ReversedList<BuiltFullSetOperation>()

    var orderBy: List<Ordinal<*>> = emptyList()

    var offset: Int = 0
    var limit: Int? = null

    fun columnsUnnamed(): Boolean = head.columnsUnnamed()

    override fun populateScope(scope: Scope) {
        head.columns.forEach { scope.internal(it) }
    }

    fun populateCtes(scope: Scope) {
        // TODO get rid of this after moving to post-select CTEs
        (head as? BuiltSelectQuery)?.body?.withs?.forEach {
            scope.cte(it.cte, it.query.columns)
        }
    }

    fun fixUnionedSelects() {
        if (unioned.isEmpty()) return

        val allReferences = linkedSetOf<Reference<*>>()

        allReferences.addAll(head.columns)
        unioned.forEach { allReferences.addAll(it.body.columns) }

        head.changeColumnsToFit(allReferences)
        unioned.forEach { it.body.changeColumnsToFit(allReferences) }
    }

    companion object {
        fun from(builder: FullQueryBuilder): BuiltFullQuery =
            unfoldBuilder(builder, BuiltFullQuery()) { it.buildIntoFullQuery() }
                .apply { fixUnionedSelects() }
    }
}