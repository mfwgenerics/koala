package mfwgenerics.kotq.query

import mfwgenerics.kotq.*

data class QueryRelation(
    val relation: Relation,
    val alias: Alias?
)

data class QueryWith(
    val type: WithType
)

data class QueryJoin(
    val type: JoinType,
    val on: Expr<Boolean>
)

data class QueryFrom(
    var relation: QueryRelation? = null,

    val withs: MutableList<QueryWith> = arrayListOf(),
    val joins: MutableList<QueryJoin> = arrayListOf()
)

data class QueryWhere(
    val from: QueryFrom = QueryFrom(),
    var where: Expr<Boolean>? = null
)

data class SelectBody(
    val where: QueryWhere = QueryWhere(),

    var groupBy: List<Expr<*>> = arrayListOf(),
    var having: Expr<Boolean>? = null
)

data class SelectQuery(
    val body: SelectBody = SelectBody(),

    var orderBy: List<Ordinal<*>> = arrayListOf(),

    var offset: Int = 0,
    var limit: Int? = null,

    var locking: LockMode? = null,

    var selected: List<ReferenceGroup> = emptyList()
)

data class DeleteStatement(
    val from: QueryFrom = QueryFrom(),
    var where: Expr<Boolean>? = null
)