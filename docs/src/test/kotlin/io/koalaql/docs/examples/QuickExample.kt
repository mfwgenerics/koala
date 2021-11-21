package io.koalaql.docs.examples

import io.koalaql.ddl.DATE
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.eq
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.h2.H2DataSource
import java.sql.DriverManager
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/* SHOW */

/*
---
title: Quick Example
custom_edit_url: https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/docs/Example.kt
sidebar_position: 1
---
*/

/* HIDE */

class QuickExample {
    /* SHOW */
    object ShopTable: Table("Shop") {
        val id = column("id", INTEGER.autoIncrement().primaryKey())

        val name = column("name", VARCHAR(256))
        val address = column("address", VARCHAR(512))

        val established = column("established", DATE.nullable())
    }

    /* HIDE */
    @Test
    /* SHOW */
    fun main() {
        val db = H2DataSource(
            provider = {
                DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            }
        )

        db.declareTables(ShopTable)

        val id = ShopTable
            .insert(values(
                rowOf(
                    ShopTable.name setTo "Helen's Hardware",
                    ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                    ShopTable.established setTo LocalDate.parse("1991-02-20")
                )
            ))
            .generatingKey(ShopTable.id)
            .perform(db)
            .single()

        val row = ShopTable
            .where(ShopTable.id eq id)
            .perform(db)
            .single()

        assertEquals("Helen's Hardware", row[ShopTable.name])
    }

    /* HIDE */
}
