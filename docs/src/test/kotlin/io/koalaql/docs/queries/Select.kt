package io.koalaql.docs.queries

import assertListEquals
import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.dsl.*
import io.koalaql.values.ResultRow
import io.koalaql.values.RowOfTwo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/* SHOW */
/*
---
title: Select
custom_edit_url: https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/docs/queries/Select.kt
sidebar_position: 1
---
*/
/* HIDE */

class Select {
    @Test
    fun selectAlls() = with(ExampleDatabase()) {
        /* SHOW */
        /*
        ### Selecting all columns

        `.selectAll()` will select all columns from a query.

        */

        val allSelected = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .selectAll()
            .perform(db)

        val row = allSelected.single()

        assertEquals("Helen's Hardware Store", row[ShopTable.name])
        assertListEquals(ShopTable.columns, allSelected.columns)

        /*
        In most cases `.selectAll()` can be omitted:
         */

        val implicitSelectAll = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .perform(db)

        assertListEquals(allSelected.columns, implicitSelectAll.columns)

        /*
        :::info

        `SELECT * FROM` will never appear in generated SQL.
        The generated SQL for these queries will name columns explicitly.

        :::
         */

        /* HIDE */
    }

    @Test
    fun selectPair() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Individual columns

        Selecting a small number of fixed columns gives you a specialized
        query with statically typed rows of ordered columns.
        You can use Kotlin's destructuring or call positional methods
        methods to access these fields in a type safe way.
        */

        val row: RowOfTwo<String, String> = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable.name, ShopTable.address) // select a pair
            .perform(db)
            .single()

        val (name, address) = row // destructuring support

        assertEquals(
            name,
            row.first() // using positional methods also works
        )

        assertEquals(row[ShopTable.name], name)
        assertEquals(row[ShopTable.address], address)

        assertEquals("Helen's Hardware Store @ 63 Smith Street, Caledonia, 62281D", "$name @ $address")

        /* HIDE */
    }

    @Test
    fun selectFromTable() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### All columns of a Table

        Passing a `Table` to `.select` will automatically select all fields from that `Table`:

         */

        val hardwareCustomers = ShopTable
            .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
            .where(ShopTable.id eq hardwareStoreId)
            .orderBy(CustomerTable.name)
            .select(CustomerTable) // select only fields from CustomerTable
            .perform(db)

        assertListEquals(CustomerTable.columns, hardwareCustomers.columns)

        /*
        You can pass multiple `Table`s to select:
         */

        ShopTable
            .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
            .select(ShopTable, CustomerTable)

        /* HIDE */
    }

    @Test
    fun explicitLabel() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Expressions and labels

        Expressions can be selected by using `as_` to label them.
        You can use an existing column name as a label or create an anonymous label:
         */

        val lowercaseName = label<String>()

        /*
        Here we select `LOWER(ShopTable.name)` in SQL and label it:
        */

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(
                ShopTable,
                lower(ShopTable.name) as_ lowercaseName
            )
            .perform(db)
            .single()

        /*
        The label can then be used to access the result of the expression:
         */

        assertEquals("helen's hardware store", row[lowercaseName])
        assertEquals(row[ShopTable.name].lowercase(), row[lowercaseName])

        /* HIDE */
    }

    @Test
    fun labeledExpr() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        For convenience you can use the labeled expression to refer to the label.
        Here is another way of writing the code from above:
         */

        val lowerName = lower(ShopTable.name) as_ label()

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable, lowerName)
            .perform(db)
            .single()

        assertEquals("helen's hardware store", row[lowerName])
        assertEquals(row[ShopTable.name].lowercase(), row[lowerName])

        /* HIDE */
    }

    @Test
    fun selectEmpty() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Empty selections
         */

        val emptySelect = ShopTable
            .where(ShopTable.id inValues listOf(hardwareStoreId, groceryStoreId))
            .select()
            .perform(db)

        assertEquals(0, emptySelect.columns.size)

        val rowCount = emptySelect
            .count()

        assertEquals(2, rowCount)

        /* HIDE */
    }

    @Test
    fun selectAliased() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Aliases
         */

        val alias = alias()

        val row = ShopTable
            .innerJoin(ShopTable.as_(alias), alias[ShopTable.id] eq groceryStoreId)
            .where(ShopTable.id eq hardwareStoreId)
            .perform(db)
            .single()

        assertEquals("Helen's Hardware Store", row[ShopTable.name])
        assertEquals("24 Hr Groceries", row[alias[ShopTable.name]])

        /* HIDE */
    }

    @Test
    fun expectPair() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Expecting columns

        If you know the exact columns a query will have at runtime,
        you can convert it to a query of statically typed rows.
        This is sometimes necessary when using subqueries in expressions.
        */

        val columnsList = listOf(ShopTable.address, ShopTable.name)

        val query = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(columnsList)

        val genericRow: ResultRow = query
            .perform(db)
            .single()

        val staticallyTypedRow: RowOfTwo<String, String> = query
            .expecting(ShopTable.name, ShopTable.address) // convert to statically typed query
            .perform(db)
            .single()

        assertEquals(
            genericRow[ShopTable.name],
            staticallyTypedRow.first()
        )

        /*

        This will not work if the columns sets do not match at runtime. This code will fail:

         */

        assertFails {
            ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(listOf(ShopTable.address, ShopTable.name))
                .expecting(ShopTable.name)
        }

        /* HIDE */

        Unit
    }
}