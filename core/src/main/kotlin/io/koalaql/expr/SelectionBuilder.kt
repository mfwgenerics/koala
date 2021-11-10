package io.koalaql.expr

import io.koalaql.query.Cte
import io.koalaql.query.EmptyRelation
import io.koalaql.query.Subquery
import io.koalaql.query.TableRelation
import io.koalaql.query.built.BuiltRelation

class SelectionBuilder(
    private val with: Map<Cte, List<Reference<*>>>
) {
    private val entries = linkedMapOf<Reference<*>, Expr<*>>()

    fun fromRelation(built: BuiltRelation) {
        val exports = when (val relation = built.relation) {
            is Cte -> with.getValue(relation)
            is TableRelation -> relation.columns
            is Subquery -> relation.of.columns
            is EmptyRelation -> return
        }.asSequence()

        exports.forEach {
            val ref = built.explicitAlias?.get(it)?:it

            entries.putIfAbsent(ref, ref)
        }
    }

    fun <T : Any> expression(expr: Expr<T>, name: Reference<T>) {
        entries[name] = expr
    }

    @Suppress("unchecked_cast")
    fun toList(): List<SelectedExpr<*>> =
        entries.map { (k, v) -> SelectedExpr(v as Expr<Any>, k as Reference<Any>) }
}