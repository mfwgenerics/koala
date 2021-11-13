package io.koalaql.docs.queries

import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import org.junit.Test

/* SHOW */
/*
---
title: Limits
sidebar_position: 9
---
*/
/* HIDE */

class Limits {
    @Test
    fun limits() = testExampleDatabase {
        /* SHOW */
        /*
        ### Limit
         */

        val firstByName = ShopTable
            .orderBy(ShopTable.name)
            .limit(1)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun offsets() = testExampleDatabase {
        /* SHOW */
        /*
        ### Offset
         */

        val thirdByName = ShopTable
            .orderBy(ShopTable.name)
            .offset(2)
            .limit(1)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun offsetWithoutLimit() = testExampleDatabase {
        /* SHOW */
        /*
        ### Offset without limit
         */

        ShopTable
            .orderBy(ShopTable.name)
            .offset(2)
            .perform(db)

        /* HIDE */
    }
}