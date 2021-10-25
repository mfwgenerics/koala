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

/* SHOW */

/*
---
title: Quick Example
custom_edit_url: https://github.com/mfwgenerics/koala/blob/examples/docs/src/main/kotlin/io/koalaql/docs/Example.kt
sidebar_position: 2
---

### Define the example table
*/

object ShopTable: Table("Shop") {
    val id = column("id", INTEGER.autoIncrement().primaryKey())

    val name = column("name", VARCHAR(256))

    val established = column("established", DATE.nullable())
}

/* HIDE */
fun quickExample() {
    /* SHOW */

    /*
    ### Connect to the database
    We'll use H2 in this example
    */
    val db = H2DataSource(
        provider = {
            DriverManager.getConnection("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
        }
    )

    /*
    ### Declare the table
    This should be done once after the database has been constructed
    */
    db.declareTables(ShopTable)

    /*
    ### Insert into the table
    We can read back the auto-generated id from the database
    */
    val id = ShopTable
        .insert(values(
            rowOf(
                ShopTable.name setTo "Tom's Hardware",
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
        .selectAll()
        .performWith(db)
        .single()

    assert(row[ShopTable.name] == "Tom's Hardware")

    /* HIDE */
}
