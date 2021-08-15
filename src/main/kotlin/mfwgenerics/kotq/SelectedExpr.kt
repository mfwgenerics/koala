package mfwgenerics.kotq

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Name
import mfwgenerics.kotq.expr.NameGroup

class SelectedExpr<T : Any>(
    val expr: Expr<T>,
    val name: Name<T>
): NameGroup

infix fun <T : Any> Expr<T>.named(name: Name<T>): SelectedExpr<T> =
    SelectedExpr(this, name)