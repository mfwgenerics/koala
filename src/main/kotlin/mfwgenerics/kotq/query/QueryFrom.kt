package mfwgenerics.kotq.query

import mfwgenerics.kotq.*
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.sql.Scope

sealed interface Statement

data class QueryRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?:Alias()

    fun populateScope(scope: Scope) {
        val innerScope = Scope(scope.names)

        when (relation) {
            is Table -> {
                relation.columns.forEach {
                    val aliased = it.name.buildAliased()

                    if (alias == null) {
                        scope.insert(aliased, aliased, computedAlias)
                    } else {
                        scope.insert(aliased.copyWithPrefix(computedAlias), aliased, computedAlias)
                    }

                    innerScope.insert(aliased, aliased, computedAlias)
                }
            }
        }

        scope.register(computedAlias, innerScope)
    }
}

data class QueryWith(
    val type: WithType
)

data class QueryJoin(
    val type: JoinType,
    val to: QueryRelation,
    val on: Expr<Boolean>
) {
    fun populateScope(scope: Scope) {
        to.populateScope(scope)
    }
}

class QueryWhere {
    lateinit var relation: QueryRelation

    val withs: MutableList<QueryWith> = arrayListOf()
    val joins: MutableList<QueryJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { it.populateScope(scope) }
    }
}

/* TODO add WINDOWs here */
data class SelectBody(
    val where: QueryWhere = QueryWhere(),

    var groupBy: List<Expr<*>> = arrayListOf(),
    var having: Expr<Boolean>? = null
) {
    fun populateScope(scope: Scope) {
        where.populateScope(scope)
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
    fun populateScope(scope: Scope) {
        selected.forEach {
            it.namedExprs().forEach { labeled ->
                when (labeled) {
                    is LabeledExpr -> {
                        scope.insert(labeled.name)
                    }
                    else -> { }
                }
            }
        }

        body.populateScope(scope)
    }
}

data class DeleteStatement(
    val from: QueryWhere = QueryWhere()
): Statement