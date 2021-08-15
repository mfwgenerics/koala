package mfwgenerics.kotq

import mfwgenerics.kotq.query.*

interface Queryable {
    fun subquery() = Subquery(this)

    fun buildQuery(): SelectQuery
}

interface Statement {
}

interface BuildsIntoSelect {
    fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect?
}

interface BuildsIntoSelectBody: BuildsIntoSelect {
    fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody?

    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? =
        buildIntoSelectBody(out.body)
}

interface BuildsIntoWhereQuery: BuildsIntoSelectBody {
    fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery?

    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? =
        buildIntoWhere(out.where)
}

interface BuildsIntoFromQuery: BuildsIntoWhereQuery {
    fun buildIntoFrom(out: QueryFrom): BuildsIntoFromQuery?

    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery? =
        buildIntoFrom(out.from)
}

interface Selectable: BuildsIntoSelect {
    fun select(vararg references: ReferenceGroup): Queryable =
        Select(this, references.asList())

    fun update(vararg assignments: Assignment<*>): Statement =
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
    fun union(against: Unionable) =
        SetOperation(this, against, SetOperationType.UNION, SetDistinctness.DISTINCT)
    fun unionAll(against: Unionable) =
        SetOperation(this, against, SetOperationType.UNION, SetDistinctness.ALL)

    fun intersect(against: Unionable) =
        SetOperation(this, against, SetOperationType.INTERSECTION, SetDistinctness.DISTINCT)
    fun intersectAll(against: Unionable) =
        SetOperation(this, against, SetOperationType.INTERSECTION, SetDistinctness.ALL)

    fun except(against: Unionable) =
        SetOperation(this, against, SetOperationType.DIFFERENCE, SetDistinctness.DISTINCT)
    fun exceptAll(against: Unionable) =
        SetOperation(this, against, SetOperationType.DIFFERENCE, SetDistinctness.ALL)
}

interface Windowable: Unionable, BuildsIntoSelectBody

interface Havingable: Windowable {
    fun having(having: Expr<Boolean>) = Having(this, having)
}

interface Groupable: Orderable, BuildsIntoWhereQuery {
    fun groupBy(vararg exprs: Expr<*>) = GroupBy(this, exprs.asList())

    /* Relation rather than Table e.g. self join delete may delete by alias */
    fun delete(vararg relations: Relation): Statement =
        Delete(this, relations.asList())
}

interface Whereable: Groupable {
    fun where(where: Expr<Boolean>) = Where(this, where)
}

interface Joinable: Whereable, BuildsIntoFromQuery {
    fun join(type: JoinType, to: Relation, on: Expr<Boolean>) =
        Join(this, type, to, on)

    fun innerJoin(to: Relation, on: Expr<Boolean>) =
        join(JoinType.INNER, to, on)

    fun leftJoin(to: Relation, on: Expr<Boolean>) =
        join(JoinType.LEFT, to, on)

    fun rightJoin(to: Relation, on: Expr<Boolean>) =
        join(JoinType.RIGHT, to, on)

    fun outerJoin(to: Relation, on: Expr<Boolean>) =
        join(JoinType.OUTER, to, on)
}

interface Withable: Joinable {
    fun with(query: Selectable) =
        WithQuery(this, WithType.NOT_RECURSIVE, query)
    fun withRecursive(query: Selectable) =
        WithQuery(this, WithType.RECURSIVE, query)

    fun insertSelect(query: Selectable, vararg assignment: Assignment<*>): Nothing = TODO()
}

interface Relation: Withable, ReferenceGroup, BuildsIntoFromQuery {
    override fun buildIntoFrom(out: QueryFrom): BuildsIntoFromQuery? {
        out.relation = QueryRelation(this, null)
        return null
    }
}

interface Aliasable: Relation {
    fun alias(alias: Alias) = Aliased(this, alias)
}

class Subquery(
    val of: Queryable
): Aliasable

enum class SetOperationType {
    UNION,
    INTERSECTION,
    DIFFERENCE
}

enum class SetDistinctness {
    DISTINCT,
    ALL
}

class Select(
    val of: Selectable,
    val references: List<ReferenceGroup>
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
): Statement

class Delete(
    val of: Selectable,
    val relations: List<Relation>
): Statement

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
    val query: Selectable
): Withable {
    override fun buildIntoFrom(out: QueryFrom): BuildsIntoFromQuery? {
        out.withs.add(QueryWith(type = type))
        return of
    }
}

class SetOperation(
    val of: Unionable,
    val against: Unionable,
    val type: SetOperationType,
    val distinctness: SetDistinctness
): Unionable {
    override fun buildIntoSelect(out: SelectQuery): BuildsIntoSelect? {
        error("not implemented")
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
    val to: Relation,
    val on: Expr<Boolean>
): Joinable {
    override fun buildIntoFrom(out: QueryFrom): BuildsIntoFromQuery? {
        out.joins.add(QueryJoin(
            type = type,
            on = on
        ))

        return of
    }
}

class Aliased(
    val of: Relation,
    val alias: Alias
): Relation {
    override fun buildIntoFrom(out: QueryFrom): BuildsIntoFromQuery? {
        out.relation = QueryRelation(of, alias)
        return null
    }
}

