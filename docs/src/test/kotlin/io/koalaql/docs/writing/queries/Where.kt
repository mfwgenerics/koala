package io.koalaql.docs.writing.queries

import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.eq
import kotlin.test.Test

/* SHOW */
/*
---
title: Where
sidebar_position: 3
---
*/
/* HIDE */

class Where {
    @Test
    fun chainWheres() = testExampleDatabase {
        /* SHOW */
        /*
        ### Chaining wheres
         */

        ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .where(ShopTable.name eq "Helen's Hardware Store")
            .perform(db)
            .single()

        /* HIDE */
    }
}