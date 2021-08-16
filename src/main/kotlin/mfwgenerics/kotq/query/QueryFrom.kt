package mfwgenerics.kotq.query

import mfwgenerics.kotq.*
import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.NamedExprs
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.sql.Scope

sealed interface Statement

data class QueryRelation(
    val relation: Relation,
    val alias: Alias?
) {
    fun intoNameSet(scope: Scope) {
        when (relation) {
            is Table -> {
                relation.columns.forEach {
                    scope.insert(it.name, it.symbol)
                }
            }
        }
    }
}

data class QueryWith(
    val type: WithType
)

data class QueryJoin(
    val type: JoinType,
    val on: Expr<Boolean>
)

class QueryWhere {
    lateinit var relation: QueryRelation

    val withs: MutableList<QueryWith> = arrayListOf()
    val joins: MutableList<QueryJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    fun intoNameSet(scope: Scope) {
        relation.intoNameSet(scope)
    }
}

/* TODO add WINDOWs here */
data class SelectBody(
    val where: QueryWhere = QueryWhere(),

    var groupBy: List<Expr<*>> = arrayListOf(),
    var having: Expr<Boolean>? = null
) {
    fun intoNameSet(scope: Scope) {
        where.intoNameSet(scope)
    }
}

data class SetOperationQuery(
    val type: SetOperationType,
    val distinctness: SetDistinctness,
    val body: SelectBody
)

data class SelectQuery(
    val body: SelectBody = SelectBody(),

    val setOperations: MutableList<SetOperationQuery> = arrayListOf(),

    var orderBy: List<Ordinal<*>> = emptyList(),

    var offset: Int = 0,
    var limit: Int? = null,

    var locking: LockMode? = null,

    var selected: List<NamedExprs> = emptyList()
): Statement {
    fun intoNameSet(scope: Scope) {
        body.intoNameSet(scope)
    }
}

data class DeleteStatement(
    val from: QueryWhere = QueryWhere()
): Statement