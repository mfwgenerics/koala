package io.koalaql.window.built

import io.koalaql.expr.Expr
import io.koalaql.expr.Ordinal
import io.koalaql.window.WindowLabel

class BuiltWindowPartitions {
    var from: WindowLabel? = null

    var partitions: List<Expr<*>> = emptyList()
    var orderBy: List<Ordinal<*>> = emptyList()
}

