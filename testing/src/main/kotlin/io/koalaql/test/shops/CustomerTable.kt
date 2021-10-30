package io.koalaql.test.shops

import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.dsl.keys

object CustomerTable: Table("Customer") {
    val id = column("id", INTEGER.autoIncrement())

    val firstName = column("firstName", VARCHAR(100))
    val lastName = column("lastName", VARCHAR(100))

    init {
        primaryKey(keys(id))
    }
}