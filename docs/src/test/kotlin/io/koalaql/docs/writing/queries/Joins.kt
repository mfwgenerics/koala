package io.koalaql.docs.writing.queries

import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.eq
import kotlin.test.Test

/* SHOW */
/*
---
title: Joins
sidebar_position: 2
---
*/
/* HIDE */

class Joins {
    @Test
    fun innerJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Inner join
         */

        CustomerTable
            .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun leftJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Left and right join
         */

        CustomerTable
            .leftJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        CustomerTable
            .rightJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun crossJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Cross join
         */

        ShopTable
            .crossJoin(CustomerTable)
            .perform(db)

        /* HIDE */
    }
}