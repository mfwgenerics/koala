package io.koalaql.docs.usage

import io.koalaql.docs.ShopTable
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.h2.H2DataSource
import io.koalaql.transact
import io.koalaql.values.ResultRow
import java.sql.DriverManager
import java.time.LocalDate


/* SHOW */
/*
---
title: Result Sets
custom_edit_url: https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/docs/usage/Results.kt
sidebar_position: 2
---
*/
/* HIDE */

fun rowExamples() {
    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
        }
    )

    db.declareTables(ShopTable)

    ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Tom's Hardware",
                ShopTable.established setTo LocalDate.parse("1991-02-20")
            ),
            rowOf(
                ShopTable.name setTo "24 Hr Groceries",
                ShopTable.established setTo LocalDate.parse("2007-08-02")
            )
        ))
        .performWith(db)

    db.transact { cxn ->
        /* SHOW */

        /*
        ### Working with results
        Performing a SELECT will execute the SQL query and return a sequence of results.
        Iterating through this sequence will fetch results from the underlying JDBC ResultSet.
        */

        val names = ShopTable
            .selectAll()
            .performWith(cxn)
            .map { row -> // this is Kotlin's Sequence.map
                row[ShopTable.name]
            }
            .toSet()

        assert("Tom's Hardware" in names)
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
            .performWith(cxn)
            .first()

        val nonNull: String = row.getValue(ShopTable.name) // always expect non-null
        val nullable: String? = row.getOrNull(ShopTable.name) // never expect non-null

        row[ShopTable.name] // returns a non-null value
        row[ShopTable.established] // returns a nullable value

        assert(row[ShopTable.name] == row.getValue(ShopTable.name))

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