package mfwgenerics.kotq

sealed interface Queryable {

}

sealed interface Selectable {
    fun select(vararg references: ReferenceGroup): Queryable = TODO()
}

sealed interface Withable: Selectable {
    fun with(query: Selectable) = WithQuery(this, query)
}

sealed interface Unionable: Withable {
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

sealed interface Limitable: Unionable {
    fun limit(rows: Int) = Limit(this, rows)
}

sealed interface Offsetable: Limitable {
    fun offset(rows: Int) = Offset(this, rows)
}

sealed interface Orderable: Offsetable {
    fun orderBy(vararg ordinals: Ordinal<*>) = OrderBy(this, ordinals.asList())
}

sealed interface Havingable: Orderable {
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

sealed interface Relation: Joinable, ReferenceGroup

sealed interface Aliasable: Relation {
    fun alias(alias: Alias) = Aliased(this, alias)
}

enum class SetOperationType {
    UNION,
    INTERSECTION,
    DIFFERENCE
}

enum class SetDistinctness {
    DISTINCT,
    ALL
}

class WithQuery(
    val of: Withable,
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

