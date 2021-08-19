package mfwgenerics.kotq

import mfwgenerics.kotq.expr.*
import mfwgenerics.kotq.query.*
import mfwgenerics.kotq.sql.StandardSql
import mfwgenerics.kotq.window.LabeledWindow

interface BuildsIntoSelect {
    fun buildSelect(): BuiltSelectQuery =
        unfoldBuilder(BuiltSelectQuery()) { buildIntoSelect(it) }

    fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect?
}

interface Queryable: BuildsIntoSelect {
    fun subquery() = Subquery(this)
}

interface Statementable {
}

interface BuildsIntoSelectBody: BuildsIntoSelect {
    fun buildSelectBody(): SelectBody =
        unfoldBuilder(SelectBody()) { buildIntoSelectBody(it) }

    fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody?

    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? =
        buildIntoSelectBody(out.body)
}

interface BuildsIntoWhereQuery: BuildsIntoSelectBody {
    fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery?

    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? =
        buildIntoWhere(out.where)
}

interface Selectable: BuildsIntoSelect {
    fun select(vararg references: NamedExprs): Queryable =
        Select(this, references.asSequence().flatMap { it.namedExprs() }.toList())

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

interface UnionOperand: BuildsIntoSelect

interface Unionable: Orderable {
    fun union(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.DISTINCT)
    fun unionAll(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.UNION, Distinctness.ALL)

    fun intersect(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.DISTINCT)
    fun intersectAll(against: UnionableUnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.INTERSECTION, Distinctness.ALL)

    fun except(against: UnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.DISTINCT)
    fun exceptAll(against: UnionableUnionOperand): Unionable =
        SetOperation(this, against, SetOperationType.DIFFERENCE, Distinctness.ALL)
}

interface SelectedUnionOperand: Queryable, UnionOperand

interface UnionableUnionOperand: Unionable, UnionOperand, BuildsIntoSelectBody {
    override fun select(vararg references: NamedExprs): SelectedUnionOperand =
        SelectUnionableUnionOperand(this, references.asSequence().flatMap { it.namedExprs() }.toList())
}

private class SelectUnionableUnionOperand(
    val of: UnionableUnionOperand,
    val references: List<Labeled<*>>
): SelectedUnionOperand {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.selected = references
        return of
    }
}

interface Windowable: UnionableUnionOperand {
    fun window(vararg windows: LabeledWindow): UnionableUnionOperand =
        WindowedQuery(this, windows.asList())
}

private class WindowedQuery(
    val lhs: Windowable,
    val windows: List<LabeledWindow>
): UnionableUnionOperand {
    override fun buildIntoSelectBody(out: SelectBody): BuildsIntoSelectBody? {
        out.windows = windows
        return lhs
    }
}

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
    fun with(vararg queries: AliasedQueryable) =
        WithQuery(this, WithType.NOT_RECURSIVE, queries.asList())
    fun withRecursive(vararg queries: AliasedQueryable) =
        WithQuery(this, WithType.RECURSIVE, queries.asList())

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
    override fun namedExprs(): List<Labeled<*>> {
        error("not implemented")
    }
}

enum class SetOperationType(
    override val sql: String
): StandardSql {
    UNION("UNION"),
    INTERSECTION("INTERSECT"),
    DIFFERENCE("EXCEPT")
}

enum class Distinctness {
    DISTINCT,
    ALL
}

class Select(
    val of: Selectable,
    val references: List<Labeled<*>>
): Queryable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.selected = references
        return of
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
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.locking = mode

        return of
    }
}

class WithQuery(
    val of: Withable,
    val type: WithType,
    val queries: List<AliasedQueryable>
): Withable {
    override fun buildIntoWhere(out: QueryWhere): BuildsIntoWhereQuery {
        out.withType = type
        out.withs = queries.map {
            BuiltWith(
                it.alias,
                it.queryable.buildSelect()
            )
        }

        return of
    }
}

class SetOperation(
    val of: Unionable,
    val against: UnionOperand,
    val type: SetOperationType,
    val distinctness: Distinctness
): Unionable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.setOperations.add(SetOperationQuery(
            type = type,
            distinctness = distinctness,
            body = against.buildSelect()
        ))

        return of
    }
}

class Limit(
    val of: Limitable,
    val rows: Int
): Lockable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.limit = rows

        return of
    }
}

class Offset(
    val of: Offsetable,
    val rows: Int
): Limitable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
        out.offset = rows

        return of
    }
}

class OrderBy(
    val of: Orderable,
    val ordinals: List<Ordinal<*>>
): Offsetable {
    override fun buildIntoSelect(out: BuiltSelectQuery): BuildsIntoSelect? {
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
