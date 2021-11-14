package io.koalaql.docs.writing.expressions

import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import org.junit.Test
import kotlin.test.assertEquals

/* SHOW */

/*
---
title: Case Expressions
sidebar_position: 1
---
*/

/* HIDE */

class CaseExpressions {
    @Test
    fun caseExpressions() = testExampleDatabase {
        /* SHOW */

        /*
        ### Case

        Case expressions are created by calling `case`.
         */

        val shopType = case(ShopTable.id,
            when_(hardwareStoreId).then("HARDWARE"),
            when_(groceryStoreId).then("GROCERIES"),
            else_ = value("OTHER")
        ) as_ label()

        val type = ShopTable
            .where(ShopTable.id eq groceryStoreId)
            .select(shopType)
            .perform(db)
            .single()
            .getValue(shopType)

        assertEquals(type, "GROCERIES")

        /* HIDE */
    }

    @Test
    fun emptyCase() = testExampleDatabase {
        /* SHOW */

        /*
        ### Empty case

        The subject of a case expression can be omitted.
        The case expression will then match the first true condition
         */

        val shopType = case(
            when_(ShopTable.id eq hardwareStoreId).then("HARDWARE"),
            when_(ShopTable.id eq groceryStoreId).then("GROCERIES")
        ) as_ label()

        val type = ShopTable
            .where(ShopTable.id eq groceryStoreId)
            .select(shopType)
            .perform(db)
            .single()
            .getValue(shopType)

        assertEquals(type, "GROCERIES")

        /* HIDE */
    }
}