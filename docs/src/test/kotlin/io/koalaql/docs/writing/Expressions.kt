package io.koalaql.docs.writing

import assertGeneratedSql
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import org.junit.Test
import kotlin.test.assertEquals

/* SHOW */
/*
---
title: Expressions
sidebar_position: 6
---
*/
/* HIDE */

/* SHOW */
/* ## Case Expressions */
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

        assertGeneratedSql("""
            SELECT CASE T0."id"
            WHEN 1 THEN 'HARDWARE'
            WHEN 2 THEN 'GROCERIES'
            ELSE 'OTHER'
            END c0
            FROM "Shop" T0
            WHERE T0."id" = 2
        """)

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

/* SHOW */
/* ## String Operations */
/* HIDE */

class StringOperations {
    @Test
    fun likeAndNotLike() = testExampleDatabase {
        /* SHOW */

        /*
        ### Like
         */

        val row = ShopTable
            .where(ShopTable.name like "%Hardware%")
            .where(ShopTable.name notLike "%Groceries%")
            .perform(db)
            .single()

        assertEquals(hardwareStoreId, row.getValue(ShopTable.id))

        assertGeneratedSql("""
            SELECT T0."id" c0
            , T0."name" c1
            , T0."address" c2
            , T0."established" c3
            FROM "Shop" T0
            WHERE (T0."name" LIKE '%Hardware%') AND (T0."name" NOT LIKE '%Groceries%')
        """)

        /* HIDE */
    }

    @Test
    fun upperAndLower() = testExampleDatabase {
        /* SHOW */

        /*
        ### Upper and lower
         */

        val (lowerName, upperName) = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(
                lower(ShopTable.name) as_ label(),
                upper(ShopTable.name) as_ label(),
            )
            .perform(db)
            .single()

        assertEquals("helen's hardware", lowerName)
        assertEquals("HELEN'S HARDWARE", upperName)

        assertGeneratedSql("""
            SELECT LOWER(T0."name") c0
            , UPPER(T0."name") c1
            FROM "Shop" T0
            WHERE T0."id" = 1
        """)

        /* HIDE */
    }
}