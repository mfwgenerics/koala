package io.koalaql.docs

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
sidebar_position: 2
---

### Define a table
*/

object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val name = column("name", VARCHAR(256))
    val address = column("address", VARCHAR(512))

    val established = column("established", DATE.nullable())
}

/* HIDE */

class Example {
    @Test
    fun quickExample() {
        /* SHOW */

        /*
        ### Connect to your database
        We use H2 in this example
        */
        val db = H2DataSource(
            provider = {
                DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            }
        )

        /*
        ### Declare the table
        This should be done for all tables at startup
        */
        db.declareTables(ShopTable)

        /*
        ### Insert into the table
        We also read back the auto-generated id from the database
        */
        val id = ShopTable
            .insert(values(
                rowOf(
                    ShopTable.name setTo "Helen's Hardware Store",
                    ShopTable.address setTo "63 Smith Street, Caledonia, 62281D",
                    ShopTable.established setTo LocalDate.parse("1991-02-20")
                )
            ))
            .generatingKey(ShopTable.id)
            .performWith(db)
            .single()

        /*
        ### Select from the table
         */
        val row = ShopTable
            .where(ShopTable.id eq id)
            .performWith(db)
            .single()

        assertEquals("Helen's Hardware Store", row[ShopTable.name])

        /* HIDE */
    }
}
