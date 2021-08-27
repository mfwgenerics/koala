package mfwgenerics.kotq.expr

import mfwgenerics.kotq.query.Cte
import mfwgenerics.kotq.query.Relation
import mfwgenerics.kotq.query.Relvar
import mfwgenerics.kotq.query.Subquery

class SelectionBuilder {
    val entries = linkedMapOf<Reference<*>, Expr<*>>()

    fun fromAll() {

    }

    fun fromRelation(relation: Relation) {
        val exports = when (relation) {
            is Cte -> TODO()
            is Relvar -> relation.columns.asSequence()
            is Subquery -> relation.of.columns.values.asSequence()
        }

        exports.forEach { entries.putIfAbsent(it, it) }
    }

    fun <T : Any> expression(expr: Expr<T>, name: Reference<T>) {
        entries[name] = expr
    }
}