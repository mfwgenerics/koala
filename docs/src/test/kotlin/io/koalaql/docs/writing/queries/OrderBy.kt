package io.koalaql.docs.writing.queries

import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import org.junit.Test

/* SHOW */
/*
---
title: Order By
sidebar_position: 8
---
*/
/* HIDE */

class OrderBy {
    @Test
    fun orderBy() = testExampleDatabase {
        /* SHOW */
        /*
        ### Order by
         */

        val alphabetical = ShopTable
            .orderBy(ShopTable.name)
            .perform(db)

        val reverseAlphabetical = ShopTable
            .orderBy(ShopTable.name.desc())
            .perform(db)

        /* HIDE */
    }

    @Test
    fun orderByDesc() = testExampleDatabase {
        /* SHOW */
        /*
        ### Nulls first and last
         */

        ShopTable
            .orderBy(ShopTable.established.desc().nullsLast())
            .perform(db)

        ShopTable
            .orderBy(ShopTable.established.asc().nullsFirst())
            .perform(db)

        /* HIDE */
    }

    @Test
    fun multiOrderBy() = testExampleDatabase {
        /* SHOW */
        /*
        ### Compound order
         */

        ShopTable
            .orderBy(
                ShopTable.established.desc().nullsLast(),
                ShopTable.name
            )
            .perform(db)

        /* HIDE */
    }
}