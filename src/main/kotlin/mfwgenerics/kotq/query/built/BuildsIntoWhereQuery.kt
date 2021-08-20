package mfwgenerics.kotq.query.built

interface BuildsIntoWhereQuery: BuildsIntoSelectBody {
    fun buildIntoWhere(out: BuiltWhere): BuildsIntoWhereQuery?

    override fun buildIntoSelectBody(out: BuiltSelectBody): BuildsIntoSelectBody? =
        buildIntoWhere(out.where)
}