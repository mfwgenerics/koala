package mfwgenerics.kotq.query

import mfwgenerics.kotq.*
import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.sql.Scope
import mfwgenerics.kotq.values.RowSequence
import mfwgenerics.kotq.window.LabeledWindow

sealed interface Statement

class BuiltInsert: Statement {
    lateinit var relation: QueryRelation

    var withType: WithType = WithType.NOT_RECURSIVE
    var withs: List<BuiltWith> = emptyList()

    lateinit var query: BuiltQuery

    fun populateScope(scope: Scope) {

    }
}

data class QueryRelation(
    val relation: Relation,
    val alias: Alias?
) {
    val computedAlias = alias?:Alias()

    fun populateScope(scope: Scope) {
        when (relation) {
            is Relvar -> {
                val innerScope = scope.innerScope()

                relation.columns.forEach {
                    innerScope.external(it, it.symbol)
                }

                innerScope.externals().forEach { name ->
                    if (alias == null) {
                        scope.internal(name, name, computedAlias)
                    } else {
                        scope.internal(computedAlias[name], name, computedAlias)
                    }
                }

                scope.register(computedAlias, innerScope)
            }
            is Subquery -> {
                val innerScope = scope.innerScope()

                relation.of.populateScope(innerScope)

                innerScope.externals().forEach { name ->
                    if (alias == null) {
                        scope.internal(name, name, computedAlias)
                    } else {
                        scope.internal(computedAlias[name], name, computedAlias)
                    }
                }

                scope.register(computedAlias, innerScope)
            }
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
    val query: BuiltQuery
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

            innerScope.externals().forEach { name ->
                scope.internal(it.alias[name], name, it.alias)
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

sealed interface BuiltQuery: Statement {
    val columns: LabelList

    fun populateScope(scope: Scope)
}

data class BuiltValuesQuery(
    val values: RowSequence
): BuiltQuery {
    override val columns: LabelList get() = values.columns

    override fun populateScope(scope: Scope) {
        columns.values.forEach { scope.external(it) }
    }
}

data class BuiltSelectQuery(
    val body: SelectBody = SelectBody(),

    val setOperations: MutableList<SetOperationQuery> = arrayListOf(),

    var orderBy: List<Ordinal<*>> = emptyList(),

    var offset: Int = 0,
    var limit: Int? = null,

    var locking: LockMode? = null,

    var selected: List<Labeled<*>> = emptyList()
): BuiltQuery, Statement {
    override lateinit var columns: LabelList

    override fun populateScope(scope: Scope) {
        body.populateScope(scope)

        selected.forEach {
            it.namedExprs().forEach { labeled ->
                if (labeled.expr != labeled.name) scope.internal(labeled.name)
                scope.external(labeled.name)
            }
        }
    }
}

data class DeleteStatement(
    val from: QueryWhere = QueryWhere()
): Statement