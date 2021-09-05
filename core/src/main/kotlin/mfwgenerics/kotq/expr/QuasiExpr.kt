package mfwgenerics.kotq.expr

import mfwgenerics.kotq.dialect.ExpressionCompiler

sealed interface QuasiExpr {
    fun compile(emitParens: Boolean, compiler: ExpressionCompiler)
}