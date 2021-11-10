package io.koalaql.docs.statements

import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.tables.ShopTable
import org.junit.Test
import kotlin.test.assertEquals

/* SHOW */
/*
---
title: Select
sidebar_position: 1
---
*/
/* HIDE */

class Update {
    @Test
    fun emptyUpdate() = with(ExampleDatabase()) {
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