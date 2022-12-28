package io.koalaql

import io.koalaql.ddl.Table
import io.koalaql.ddl.Table.Companion.primaryKey
import io.koalaql.ddl.VARCHAR
import kotlin.test.Test
import kotlin.test.assertEquals

class TableTests {
    class DuplicateNameTable: Table("Test") {
        val name0 = column("name", VARCHAR(64))
        val name1 = column("name", VARCHAR(64))
    }

    class MultiplePrimaryKeysTable: Table("Test") {
        val name0 = column("name0", VARCHAR(64).primaryKey())
        val name1 = column("name1", VARCHAR(64))

        init {
            primaryKey(name0, name1)
        }
    }

    @Test
    fun `table duplicate field error message`() {
        try {
            DuplicateNameTable()
        } catch (ex: Exception) {
            assertEquals("io.koalaql.TableTests\$DuplicateNameTable: field name \"name\" is already in use", ex.message)
        }
    }

    @Test
    fun `table multiple primary keys error message`() {
        try {
            MultiplePrimaryKeysTable()
        } catch (ex: Exception) {
            assertEquals(
                "io.koalaql.TableTests\$MultiplePrimaryKeysTable: could not create the primary key Test_name0_name1_pkey because there is already a primary key Test_name0_pkey",
                ex.message
            )
        }
    }
}