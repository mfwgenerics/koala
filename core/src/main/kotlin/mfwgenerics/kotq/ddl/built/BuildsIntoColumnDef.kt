package mfwgenerics.kotq.ddl.built

import mfwgenerics.kotq.unfoldBuilder

interface BuildsIntoColumnDef {
    fun buildColumnDef(): BuiltColumnDef =
        unfoldBuilder(BuiltColumnDef()) { buildIntoColumnDef(it) }

    fun buildIntoColumnDef(out: BuiltColumnDef): BuildsIntoColumnDef?
}