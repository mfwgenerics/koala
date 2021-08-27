package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.query.Relation
import mfwgenerics.kotq.query.Relvar
import mfwgenerics.kotq.query.Subquery
import mfwgenerics.kotq.query.built.BuiltRelation

class SelectionBuilder {
    val entries = linkedMapOf<Reference<*>, Expr<*>>()

    fun fromAll() {

    }

    fun fromRelation(built: BuiltRelation) {
        val exports = when (val relation = built.relation) {
            is Cte -> TODO()
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
}