package io.koalaql.docs.queries

import assertListEquals
import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.dsl.*
import kotlin.test.Test
import kotlin.test.assertEquals

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
        ### All columns
        */

        val selectAll = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .selectAll() // select all columns
            .perform(db)

        val row = selectAll.single()

        assertEquals("Helen's Hardware Store", row[ShopTable.name])

        val implicitSelectAll = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .perform(db)

        assertListEquals(selectAll.columns, implicitSelectAll.columns)

        /*
        :::info

        `SELECT *` will never appear in generated SQL.
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
        */

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable.name, ShopTable.address) // select a pair
            .perform(db)
            .single()

        val (name, address) = row

        assertEquals(row[ShopTable.name], name)
        assertEquals(row[ShopTable.address], address)

        assertEquals("Helen's Hardware Store @ 63 Smith Street, Caledonia, 62281D", "$name @ $address")

        /* HIDE */
    }

    @Test
    fun selectFromTable() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Columns of a single table
         */

        val hardwareCustomers = ShopTable
            .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
            .where(ShopTable.id eq hardwareStoreId)
            .orderBy(CustomerTable.name)
            .select(CustomerTable) // select only fields from CustomerTable
            .perform(db)
            .map { row -> row[CustomerTable.name] }
            .toList()

        /* HIDE */
    }

    @Test
    fun explicitLabel() = with(ExampleDatabase()) {
        /* SHOW */

        /*
        ### Expressions and labels
         */

        val lowerName = label<String>()

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable, lower(ShopTable.name) as_ lowerName)
            .perform(db)
            .single()

        assertEquals("helen's hardware store", row[lowerName])
        assertEquals(row[ShopTable.name].lowercase(), row[lowerName])

        /* HIDE */
    }

    @Test
    fun labeledExpr() = with(ExampleDatabase()) {
        /* SHOW */

        val lowerName = lower(ShopTable.name) as_ label() // label<String>() is inferred

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
}