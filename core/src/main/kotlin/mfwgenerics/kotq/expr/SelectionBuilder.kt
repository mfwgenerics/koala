package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.query.built.BuiltRelation

class SelectionBuilder(
    private val with: Map<Cte, LabelList>
) {
    private val entries = linkedMapOf<Reference<*>, Expr<*>>()

    fun fromRelation(built: BuiltRelation) {
        val exports = when (val relation = built.relation) {
            is Cte -> with.getValue(relation).values
            is Relvar -> relation.columns
            is Subquery -> relation.of.columns.values
            is Values -> relation.columns.values
            is EmptyRelation -> return
        }.asSequence()

        exports.forEach {
            val ref = built.alias?.get(it)?:it

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