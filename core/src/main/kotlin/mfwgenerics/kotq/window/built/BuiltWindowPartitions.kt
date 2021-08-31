package mfwgenerics.kotq.window.built

import mfwgenerics.kotq.expr.Expr
import mfwgenerics.kotq.expr.Ordinal
import mfwgenerics.kotq.window.WindowLabel

class BuiltWindowPartitions {
    var from: WindowLabel? = null

    var partitions: List<Expr<*>> = emptyList()
    var orderBy: List<Ordinal<*>> = emptyList()
}

