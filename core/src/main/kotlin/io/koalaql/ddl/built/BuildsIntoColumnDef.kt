package io.koalaql.ddl.built

import io.koalaql.unfoldBuilder

interface BuildsIntoColumnDef {
    fun buildColumnDef(): BuiltColumnDef =
        unfoldBuilder(BuiltColumnDef()) { buildIntoColumnDef(it) }

    fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef?
}