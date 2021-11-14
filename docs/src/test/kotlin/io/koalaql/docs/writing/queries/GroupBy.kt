package io.koalaql.docs.writing.queries

import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import kotlin.test.Test

/* SHOW */
/*
---
title: Group By
sidebar_position: 4
---
*/
/* HIDE */

class GroupBy {
    @Test
    fun countingGroupBy() = testExampleDatabase {
        /* SHOW */
        /*
        ### Group by
         */

        val customerCount = label<Int>()

        CustomerTable
            .groupBy(CustomerTable.shop)
            .select(CustomerTable.shop, count() as_ customerCount)
            .perform(db)

        /* HIDE */
    }
}