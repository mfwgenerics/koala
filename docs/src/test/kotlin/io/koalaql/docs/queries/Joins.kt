package io.koalaql.docs.queries

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

        CustomerTable
            .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)

        /* HIDE */
    }
}