package io.koalaql.docs.executing

import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.tables.ShopTable
import io.koalaql.transact
import io.koalaql.values.ResultRow
import kotlin.test.Test
import kotlin.test.assertEquals


/* SHOW */
/*
---
title: Result Sets
custom_edit_url: https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/docs/usage/Results.kt
sidebar_position: 2
---
*/
/* HIDE */

class Results {
    @Test
    fun rowExamples() {
        val db = ExampleDatabase().db

        db.transact { cxn ->
            /* SHOW */

            /*
            ### Working with results
            SQL queries return sequences of result rows.
            Iterating through the sequence will fetch results from the underlying JDBC ResultSet.
            */

            val names = ShopTable
                .selectAll()
                .perform(cxn)
                .map { row -> // this is Kotlin's Sequence.map
                    row[ShopTable.name]
                }
                .toSet()

            assert("Helen's Hardware" in names)
            assert("24 Hr Groceries" in names)

            /*
            :::info

            Once a result sequence has been fully iterated the underlying JDBC resources are closed.
            Unclosed resources will remain open until the connection is closed at the end of the transaction.

            :::

            ### Indexing into rows
            Directly indexing into a row by a non-nullable column will always expect a non-null value.
            Indexing by anything other than a non-nullable column will return a nullable value.

            Rows support `getValue` and `getOrNull` operations for explicitly dealing with null values:
            */

            val row: ResultRow = ShopTable
                .selectAll()
                .perform(cxn)
                .first()

            val nonNull: String = row.getValue(ShopTable.name) // always expect non-null
            val nullable: String? = row.getOrNull(ShopTable.name) // never expect non-null

            row[ShopTable.name] // returns a non-null value
            row[ShopTable.established] // returns a nullable value

            assertEquals(row[ShopTable.name], row.getValue(ShopTable.name))

            /*
            :::caution

            Some queries may return null values for non-nullable columns.
            A common cause of this is selecting from a left joined table.
            In these circumstances, use `getOrNull` explicitly to retrieve the column values.

            :::
             */

            /* HIDE */
        }
    }
}