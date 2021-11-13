package io.koalaql.docs.statements

import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import org.junit.Test
import kotlin.test.assertEquals

/* SHOW */
/*
---
title: Updates
sidebar_position: 1
---
*/
/* HIDE */

class Update {
    @Test
    fun emptyUpdate() = testExampleDatabase {
        /* SHOW */
        /*
        ### Empty updates
        */

        val updated = ShopTable
            .update()
            .perform(db)

        assertEquals(0, updated)

        /* HIDE */
    }
}