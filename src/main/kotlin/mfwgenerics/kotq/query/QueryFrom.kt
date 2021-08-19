package mfwgenerics.kotq.query

import mfwgenerics.kotq.*
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.window.LabeledWindow

sealed interface Statement

data class QueryRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?:Alias()

    fun populateScope(scope: Scope) {
        val innerScope = scope.innerScope()

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
            is Subquery -> TODO()
        }

        scope.register(computedAlias, innerScope)
    }
}

data class QueryJoin(
    val type: JoinType,
    val to: QueryRelation,
    val on: Expr<Boolean>
) {
    fun populateScope(scope: Scope) {
        to.populateScope(scope)
    }
}

class BuiltWith(
    val alias: Alias,
    val query: BuiltSelectQuery
)

class QueryWhere {
    lateinit var relation: QueryRelation

    var withType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    val joins: MutableList<QueryJoin> = arrayListOf()

    var where: Expr<Boolean>? = null

    fun populateScope(scope: Scope) {
        relation.populateScope(scope)

        joins.forEach { it.populateScope(scope) }

        withs.forEach {
            val innerScope = scope.innerScope()

            it.query.populateScope(innerScope)

            innerScope.allNames().forEach { name ->
                scope.insert(name.copyWithPrefix(it.alias), name, it.alias)
            }

            scope.register(it.alias, innerScope)
        }
    }
}

data class SelectBody(
    val where: QueryWhere = QueryWhere(),

    var groupBy: List<Expr<*>> = arrayListOf(),
    var having: Expr<Boolean>? = null,

    var windows: List<LabeledWindow> = emptyList()
) {
    fun populateScope(scope: Scope) {
        where.populateScope(scope)
    }
}

data class SetOperationQuery(
    val type: SetOperationType,
    val distinctness: Distinctness,
    val body: BuiltSelectQuery
)

data class BuiltSelectQuery(
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