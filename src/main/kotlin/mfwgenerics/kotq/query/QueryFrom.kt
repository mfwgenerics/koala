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
        when (relation) {
            is Table -> {
                val innerScope = scope.innerScope()

                relation.columns.forEach {
                    val aliased = it.buildAliased()

                    innerScope.external(aliased, it.symbol)
                }

                innerScope.allNames().forEach { name ->
                    if (alias == null) {
                        scope.insert(name, name, computedAlias)
                    } else {
                        scope.insert(name.copyWithPrefix(computedAlias), name, computedAlias)
                    }
                }

                scope.register(computedAlias, innerScope)
            }
            is Subquery -> TODO()
        }
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

    var selected: List<Labeled<*>> = emptyList()
): Statement {
    fun populateScope(scope: Scope) {
        body.populateScope(scope)

        selected.forEach {
            it.namedExprs().forEach { labeled ->
                scope.insert(labeled.name)
            }
        }
    }
}

data class DeleteStatement(
    val from: QueryWhere = QueryWhere()
): Statement