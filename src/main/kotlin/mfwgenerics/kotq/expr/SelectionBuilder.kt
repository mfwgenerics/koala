package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.query.LabelList
import mfwgenerics.kotq.query.Relvar
import mfwgenerics.kotq.query.Subquery
import mfwgenerics.kotq.query.built.BuiltRelation

class SelectionBuilder(
    private val with: Map<Cte, LabelList>
) {
    private val entries = linkedMapOf<Reference<*>, Expr<*>>()

    fun fromRelation(built: BuiltRelation) {
        val exports = when (val relation = built.relation) {
            is Cte -> with.getValue(relation).values.asSequence()
            is Relvar -> relation.columns.asSequence()
            is Subquery -> relation.of.columns.values.asSequence()
        }

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