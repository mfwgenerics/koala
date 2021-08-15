package mfwgenerics.kotq

sealed interface Queryable {
    fun subquery() = Subquery(this)
}

sealed interface Lockable: Queryable {
    fun forUpdate(): Queryable = LockQuery(this, LockMode.UPDATE)
    fun forShare(): Queryable = LockQuery(this, LockMode.SHARE)
}

sealed interface Statement {
}

sealed interface Selectable {
    fun select(vararg references: ReferenceGroup): Lockable =
        Select(this, references.asList())

    fun update(vararg assignments: Assignment<*>): Statement =
        Update(this, assignments.asList())

    /* Relation rather than Table e.g. self join delete may delete by alias */
    fun delete(vararg relations: Relation): Statement =
        Delete(this, relations.asList())
}

sealed interface Limitable: Selectable {
    fun limit(rows: Int) = Limit(this, rows)
}

sealed interface Offsetable: Limitable {
    fun offset(rows: Int) = Offset(this, rows)
}

sealed interface Orderable: Offsetable {
    fun orderBy(vararg ordinals: Ordinal<*>) = OrderBy(this, ordinals.asList())
}

sealed interface Unionable: Orderable {
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

sealed interface Windowable: Unionable {
}

sealed interface Havingable: Windowable {
    fun having(having: Expr<Boolean>) = Having(this, having)
}

sealed interface Groupable: Orderable {
    fun groupBy(vararg exprs: Expr<*>) = GroupBy(this, exprs.asList())
}

sealed interface Whereable: Groupable {
    fun where(where: Expr<Boolean>) = Where(this, where)
}

sealed interface Joinable: Whereable {
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

sealed interface Withable: Joinable {
    fun with(query: Selectable) =
        WithQuery(this, WithMode.NOT_RECURSIVE, query)
    fun withRecursive(query: Selectable) =
        WithQuery(this, WithMode.RECURSIVE, query)
}

sealed interface Relation: Withable, ReferenceGroup

sealed interface Aliasable: Relation {
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
): Lockable

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
): Queryable

class WithQuery(
    val of: Withable,
    val mode: WithMode,
    val query: Selectable
): Withable

class SetOperation(
    val of: Unionable,
    val against: Unionable,
    val type: SetOperationType,
    val distinctness: SetDistinctness
): Unionable

class Limit(
    val of: Limitable,
    val rows: Int
): Selectable

class Offset(
    val of: Offsetable,
    val rows: Int
): Limitable

class OrderBy(
    val of: Orderable,
    val ordinals: List<Ordinal<*>>
): Offsetable

class Having(
    val of: Havingable,
    val having: Expr<Boolean>
): Havingable

class GroupBy(
    val of: Groupable,
    val on: List<Expr<*>>
): Havingable

class Where(
    val of: Whereable,
    val where: Expr<Boolean>
): Whereable

class Join(
    val of: Joinable,
    val type: JoinType,
    val to: Relation,
    val on: Expr<Boolean>
): Joinable

class Aliased(
    val of: Relation,
    val alias: Alias
): Relation

