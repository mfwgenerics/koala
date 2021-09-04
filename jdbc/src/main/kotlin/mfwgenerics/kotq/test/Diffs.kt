package mfwgenerics.kotq.test

import mfwgenerics.kotq.ddl.built.BuiltColumnDef
import mfwgenerics.kotq.ddl.built.BuiltColumnDefault
import mfwgenerics.kotq.ddl.built.ColumnDefaultExpr
import mfwgenerics.kotq.ddl.built.ColumnDefaultValue
import mfwgenerics.kotq.ddl.diff.ColumnDefinitionDiff
import mfwgenerics.kotq.ddl.diff.Diff
import mfwgenerics.kotq.ddl.diff.SchemaDiff
import mfwgenerics.kotq.ddl.diff.TableDiff

interface DiffMatcher<in C, in A> {
    fun matchCreated(name: String, expected: C, actual: C)
    fun matchAltered(name: String, expected: A, actual: A)

    companion object {
        val IGNORE = object : DiffMatcher<Any?, Any?> {
            override fun matchCreated(name: String, expected: Any?, actual: Any?) { }
            override fun matchAltered(name: String, expected: Any?, actual: Any?) { }
        }
    }
}

private fun matchValues(name: String, expected: Any?, actual: Any?) {
    assert(expected == actual)
        { "$name: expected $expected actual: $actual" }
}

private fun matchColumnDefaults(name: String, expected: BuiltColumnDefault?, actual: BuiltColumnDefault?) {
    when (expected) {
        null -> assert(actual == null) { "$name: expected null" }
        is ColumnDefaultValue -> {
            /* use check bc assert won't downcast */
            check(actual is ColumnDefaultValue) { "$name: expected ColumnDefaultValue" }
            matchValues(name, expected.value, actual.value)
        }
        is ColumnDefaultExpr -> {
            check(actual is ColumnDefaultExpr) { "$name: expected ColumnDefaultExpr" }
            assert(expected === actual.expr) { "$name: expected Expr@${System.identityHashCode(expected.expr)}" }
        }
    }
}

object ColumnDiffMatcher: DiffMatcher<BuiltColumnDef, ColumnDefinitionDiff> {
    override fun matchCreated(name: String, expected: BuiltColumnDef, actual: BuiltColumnDef) {
        matchValues("$name.`columnType.dataType`", expected.columnType.dataType, actual.columnType.dataType)
        matchValues("$name.autoIncrement", expected.autoIncrement, actual.autoIncrement)
        matchValues("$name.notNull", expected.notNull, actual.notNull)

        matchColumnDefaults("$name.default", expected.default, actual.default)

        matchValues("$name.references", expected.references, actual.references)
    }

    override fun matchAltered(name: String, expected: ColumnDefinitionDiff, actual: ColumnDefinitionDiff) {
        matchValues("$name.`type.mappedType.dataType`", expected.type?.mappedType?.dataType, actual.type?.mappedType?.dataType)

        matchValues("$name.notNull", expected.notNull, actual.notNull)

        if (expected.changedDefault == null) {
            assert(actual.changedDefault == null) { "$name.changedDefault: expected null" }
        } else {
            assert(actual.changedDefault != null) { "$name.changedDefault: expected non-null" }

            matchColumnDefaults("$name.changedDefault.default", expected.changedDefault?.default, actual.changedDefault?.default)
        }

        matchValues("$name.isAutoIncrement", expected.isAutoIncrement, actual.isAutoIncrement)
    }
}

fun <K> Set<K>.assertMatch(name: String, actual: Set<K>) {
    val missingFromActual = this - actual
    val missingFromExpected = actual - this

    assert(missingFromActual.isEmpty()) { "$name: expected $missingFromActual" }
    assert(missingFromExpected.isEmpty()) { "$name: unexpected $missingFromExpected" }
}

fun <K, C, A> Diff<K, C, A>.assertMatch(name: String, actual: Diff<K, C, A>, matcher: DiffMatcher<C, A>) {
    created.keys.assertMatch("$name.created", actual.created.keys)
    altered.keys.assertMatch("$name.altered", actual.altered.keys)
    dropped.assertMatch("$name.dropped", actual.dropped)

    created.forEach { (key, value) ->
        matcher.matchCreated("$name.created.${key}", value, actual.created.getValue(key))
    }

    altered.forEach { (key, value) ->
        matcher.matchAltered("$name.created.${key}", value, actual.altered.getValue(key))
    }
}

fun TableDiff.assertMatch(actual: TableDiff) {
    columns.assertMatch("tables", actual.columns, ColumnDiffMatcher)
    indexes.assertMatch("indexes", actual.indexes, DiffMatcher.IGNORE)
}

fun SchemaDiff.assertMatch(actual: SchemaDiff) {
    tables.assertMatch("tables", actual.tables, DiffMatcher.IGNORE)
}