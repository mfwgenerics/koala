package mfwgenerics.kotq.query

import mfwgenerics.kotq.expr.SelectArgument
import mfwgenerics.kotq.query.built.BuiltQueryBody
import mfwgenerics.kotq.query.built.BuiltSelectQuery
import mfwgenerics.kotq.query.built.BuiltSubquery
import mfwgenerics.kotq.query.built.BuiltWith
import mfwgenerics.kotq.query.fluent.SelectedJust

class StandaloneSelect<T : Any>(
    private val references: List<SelectArgument>,
    private val includeAll: Boolean,
    private val withType: WithType = WithType.NOT_RECURSIVE,
    private val withs: List<CtedQueryable> = emptyList()
): SelectedJust<T> {
    override fun buildQuery(): BuiltSubquery = BuiltSelectQuery(
        true,
        BuiltQueryBody().also { body ->
            body.withType = withType
            body.withs = withs.map {
                BuiltWith(
                    it.cte,
                    it.queryable.buildQuery()
                )
            }
        },
        references,
        includeAll
    )
}