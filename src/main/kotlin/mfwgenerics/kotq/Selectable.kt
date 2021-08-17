package mfwgenerics.kotq

import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*

interface Queryable {
    fun subquery() = Subquery(this)

    fun buildQuery(): SelectQuery
}

interface Statementable {
}

interface BuildsIntoSelect {
    fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect?
}

interface BuildsIntoSelectBody: BuildsIntoSelect {
    fun buildSelectBody(): SelectBody =
        unfoldBuilder(SelectBody()) { buildIntoSelectBody(it) }

    fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody?

    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? =
        buildIntoSelectBody(out.body)
}

interface BuildsIntoWhereQuery: BuildsIntoSelectBody {
    fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery?

    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? =
        buildIntoWhere(out.where)
}

interface Selectable: BuildsIntoSelect {
    fun select(vararg references: NamedExprs): Queryable =
        Select(this, references.asList())

    fun update(vararg assignments: Assignment<*>): Statementable =
        Update(this, assignments.asList())
}

interface Lockable: Selectable {
    fun forUpdate(): Selectable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Selectable = LockQuery(this, LockMode.SHARE)
}

interface Limitable: Lockable {
    fun limit(rows: Int) = Limit(this, rows)
}

interface Offsetable: Limitable {
    fun offset(rows: Int) = Offset(this, rows)
}

interface Orderable: Offsetable {
    fun orderBy(vararg ordinals: Ordinal<*>) = OrderBy(this, ordinals.asList())
}

interface Unionable: Orderable {
    fun union(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.DISTINCT)
    fun unionAll(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.ALL)

    fun intersect(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.DISTINCT)
    fun intersectAll(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.ALL)

    fun except(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.DISTINCT)
    fun exceptAll(against: UnionOperandable): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.ALL)
}

interface UnionOperandable: Unionable, BuildsIntoSelectBody

interface Windowable: UnionOperandable

interface Havingable: Windowable {
    fun having(having: Expr<Boolean>) = Having(this, having)
}

interface Groupable: Windowable, Orderable, BuildsIntoWhereQuery {
    fun groupBy(vararg exprs: Expr<*>) = GroupBy(this, exprs.asList())

    /* Relation rather than Table e.g. self join delete may delete by alias */
    fun delete(vararg relations: Relation): Statementable =
        Delete(this, relations.asList())
}

interface Whereable: Groupable {
    fun where(where: Expr<Boolean>): Whereable = Where(this, where)
}

interface Joinable: Whereable {
    fun join(type: JoinType, to: AliasedRelation, on: Expr<Boolean>): Joinable =
        Join(this, type, to, on)

    fun innerJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.INNER, to, on)

    fun leftJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.LEFT, to, on)

    fun rightJoin(to: AliasedRelation, on: Expr<Boolean>): Joinable =
        join(JoinType.RIGHT, to, on)

    fun outerJoin(to: AliasedRelation, on: Expr<Boolean>) =
        join(JoinType.OUTER, to, on)
}

interface Withable: Joinable {
    fun with(query: NamedExprs) =
        WithQuery(this, WithType.NOT_RECURSIVE, query)
    fun withRecursive(query: NamedExprs) =
        WithQuery(this, WithType.RECURSIVE, query)

    fun insertSelect(query: NamedExprs, vararg assignment: Assignment<*>): Nothing = TODO()
}

interface AliasedRelation: Withable, NamedExprs, BuildsIntoWhereQuery {
    fun buildQueryRelation(): QueryRelation

    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery? {
        out.relation = buildQueryRelation()
        return null
    }
}

sealed interface Relation: AliasedRelation {
    fun alias(alias: Alias): AliasedRelation = Aliased(this, alias)

    override fun buildQueryRelation(): QueryRelation
        = QueryRelation(this, null)
}

class Subquery(
    val of: Queryable
): Relation {
    override fun namedExprs(): List<LabeledExpr<*>> {
        error("not implemented")
    }
}

enum class SetOperationType {
    UNION,
    INTERSECTION,
    DIFFERENCE
}

enum class Distinctness {
    DISTINCT,
    ALL
}

class Select(
    val of: Selectable,
    val references: List<NamedExprs>
): Queryable {
    override fun buildQuery(): SelectQuery {
        val query = SelectQuery()

        var next = of.buildIntoSelect(query)

        while (next != null) next = next.buildIntoSelect(query)

        query.selected = references

        return query
    }
}

class Update(
    val of: Selectable,
    val assignments: List<Assignment<*>>
): Statementable

class Delete(
    val of: Selectable,
    val relations: List<Relation>
): Statementable

class LockQuery(
    val of: Lockable,
    val mode: LockMode
): Selectable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        out.locking = mode

        return of
    }
}

class WithQuery(
    val of: Withable,
    val type: WithType,
    val query: NamedExprs
): Withable {
    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery? {
        out.withs.add(QueryWith(type = type))
        return of
    }
}

class SetOperation(
    val of: Unionable,
    val against: UnionOperandable,
    val type: SetOperationType,
    val distinctness: Distinctness
): Unionable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        out.setOperations.add(SetOperationQuery(
            type = type,
            distinctness = distinctness,
            body = against.buildSelectBody()
        ))

        return of
    }
}

class Limit(
    val of: Limitable,
    val rows: Int
): Lockable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        out.limit = rows

        return of
    }
}

class Offset(
    val of: Offsetable,
    val rows: Int
): Limitable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        out.offset = rows

        return of
    }
}

class OrderBy(
    val of: Orderable,
    val ordinals: List<Ordinal<*>>
): Offsetable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        out.orderBy = ordinals
        return of
    }
}

class Having(
    val of: Havingable,
    val having: Expr<Boolean>
): Havingable {
    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? {
        out.having = out.having?.and(having)?:having
        return of
    }
}

class GroupBy(
    val of: Groupable,
    val on: List<Expr<*>>
): Havingable {
    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? {
        out.groupBy = on
        return of
    }
}

class Where(
    val of: Whereable,
    val where: Expr<Boolean>
): Whereable {
    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery? {
        out.where = out.where?.and(where)?:where
        return of
    }
}

class Join(
    val of: Joinable,
    val type: JoinType,
    val to: AliasedRelation,
    val on: Expr<Boolean>
): Joinable {
    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery? {
        out.joins.add(QueryJoin(
            type = type,
            to = to.buildQueryRelation(),
            on = on
        ))

        return of
    }
}

class Aliased(
    val of: Relation,
    val alias: Alias
): AliasedRelation, NamedExprs by of {
    override fun buildQueryRelation(): QueryRelation
        = QueryRelation(of, alias)
}
