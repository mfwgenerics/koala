package mfwgenerics.kotq.expr

import mfwgenerics.kotq.IdentifierName
import mfwgenerics.kotq.data.DataType
import mfwgenerics.kotq.dialect.ExpressionCompiler
import mfwgenerics.kotq.dialect.ExpressionContext
import mfwgenerics.kotq.expr.built.BuildsIntoAggregatedExpr
import mfwgenerics.kotq.expr.built.BuiltAggregatable
import mfwgenerics.kotq.query.Alias
import mfwgenerics.kotq.query.built.BuiltSubquery
import kotlin.reflect.KClass

sealed interface QuasiExpr {
    fun compile(emitParens: Boolean, compiler: ExpressionCompiler)
}

class SubqueryExpr(
    val subquery: BuiltSubquery
): QuasiExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.subquery(emitParens, subquery)
    }
}

class ExprListExpr<T : Any>(
    val exprs: Collection<Expr<T>>
): QuasiExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        with (compiler) { listExpr(emitParens, exprs) }
    }
}

sealed interface ComparisonOperand<T : Any>: QuasiExpr

class ComparedQuery<T : Any>(
    val type: ComparedQueryType,
    val subquery: BuiltSubquery
): ComparisonOperand<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        with (compiler) { compared(emitParens, type, subquery) }
    }
}

fun interface Expr<T : Any>: ComparisonOperand<T>, Ordinal<T>, OrderableAggregatable<T> {
    override fun toOrderKey(): OrderKey<T> = OrderKey(SortOrder.ASC, this)

    fun asc() = OrderKey(SortOrder.ASC, this)
    fun desc() = OrderKey(SortOrder.DESC, this)

    override fun buildIntoAggregatable(into: BuiltAggregatable): BuildsIntoAggregatable? {
        into.expr = this
        return null
    }
}

sealed interface Reference<T : Any>: Expr<T>, SelectArgument {
    val type: KClass<T>

    val identifier: IdentifierName?

    override fun buildIntoSelection(selection: SelectionBuilder) {
        selection.expression(this, this)
    }

    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        with (compiler) { reference(emitParens, this@Reference) }
    }
}

class AliasedReference<T : Any>(
    override val type: KClass<T>,
    val of: Alias,
    val reference: Reference<T>
): Reference<T>, SelectArgument {
    override val identifier: IdentifierName? get() = null

    override fun equals(other: Any?): Boolean =
        other is AliasedReference<*> &&
        of.identifier == other.of.identifier &&
        reference == other.reference

    override fun hashCode(): Int = of.identifier.hashCode() xor reference.hashCode()
    override fun toString(): String = "${of.identifier}.${reference}"
}

abstract class NamedReference<T : Any>(
    override val type: KClass<T>,
    override val identifier: IdentifierName
): Reference<T> {
    override fun equals(other: Any?): Boolean =
        other is NamedReference<*> && identifier == other.identifier

    override fun hashCode(): Int = identifier.hashCode()
    override fun toString(): String = "$identifier"
}

interface AggregatedExpr<T : Any>: Expr<T>, BuildsIntoAggregatedExpr {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.aggregated(emitParens, buildAggregated())
    }
}

class CastExpr<T : Any>(
    val of: Expr<*>,
    val type: DataType<T>
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.cast(emitParens, of, type)
    }
}

class Literal<T : Any>(
    val type: KClass<T>,
    val value: T?
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.literal(emitParens, this@Literal)
    }
}

class OperationExpr<T : Any>(
    val type: OperationType,
    val args: Collection<QuasiExpr>
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.operation(emitParens, type, args)
    }
}