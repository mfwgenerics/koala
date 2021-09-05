package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler
import kotlin.reflect.KClass

class Literal<T : Any>(
    val type: KClass<T>,
    val value: T?
): Expr<T> {
    override fun compile(emitParens: Boolean, compiler: ExpressionCompiler) {
        compiler.literal(emitParens, this@Literal)
    }
}