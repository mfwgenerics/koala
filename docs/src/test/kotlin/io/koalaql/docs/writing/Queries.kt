package io.koalaql.docs.writing

import assertGeneratedSql
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.values.ResultRow
import io.koalaql.values.RowOfTwo
import kotlin.test.*

/* SHOW */
/*
---
title: Queries
sidebar_position: 4
---
*/
/* HIDE */


/* SHOW */
/* ## Selects */
/* HIDE */

class Selects {
    @Test
    fun selectAlls() = testExampleDatabase {
        /* SHOW */
        /*
        ### Selecting all columns

        Use `.selectAll()` to select all columns from a query.
        */

        val allSelected = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .selectAll()
            .perform(db)

        assertContentEquals(ShopTable.columns, allSelected.columns)

        assertGeneratedSql("""
            SELECT T0."id" "c0"
            , T0."name" "c1"
            , T0."address" "c2"
            , T0."established" "c3"
            FROM "Shop" T0
            WHERE T0."id" = ?
        """)

        /*
        In most cases `.selectAll()` can be omitted:
         */

        val implicitSelectAll = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .perform(db)

        assertContentEquals(allSelected.columns, implicitSelectAll.columns)

        assertGeneratedSql("""
            SELECT T0."id" "c0"
            , T0."name" "c1"
            , T0."address" "c2"
            , T0."established" "c3"
            FROM "Shop" T0
            WHERE T0."id" = ?
        """)

        /*
        :::info

        `SELECT * FROM` will never appear in generated SQL.
        The generated SQL for these queries will name columns explicitly.

        :::
         */

        /* HIDE */

        val row = allSelected.single()

        assertEquals("Helen's Hardware", row[ShopTable.name])
        assertContentEquals(ShopTable.columns, allSelected.columns)
    }

    @Test
    fun selectPair() = testExampleDatabase {
        /* SHOW */

        /*
        ### Individual columns

        Selecting a small number of fixed columns gives you a specialized
        query with statically typed rows of ordered columns.
        You can use Kotlin's destructuring or call positional
        methods to access these fields in a type safe way.
        */

        val row: RowOfTwo<String, String> = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable.name, ShopTable.address) // select a pair
            .perform(db)
            .single()

        val (name, address) = row // destructuring support

        assertEquals(
            name,
            row.first() // using positional methods also works
        )

        assertEquals(row[ShopTable.name], name)
        assertEquals(row[ShopTable.address], address)

        assertEquals("Helen's Hardware @ 63 Smith Street, Caledonia, 62281D", "$name @ $address")

        assertGeneratedSql("""
            SELECT T0."name" "c0"
            , T0."address" "c1"
            FROM "Shop" T0
            WHERE T0."id" = ?
        """)

        /* HIDE */
    }

    @Test
    fun selectFromTable() = testExampleDatabase {
        /* SHOW */

        /*
        ### All columns of a Table

        Passing a `Table` to `.select` will automatically select all fields from that `Table`:

         */

        val hardwareCustomers = ShopTable
            .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
            .where(ShopTable.id eq hardwareStoreId)
            .orderBy(CustomerTable.name)
            .select(CustomerTable) // select only fields from CustomerTable
            .perform(db)

        assertContentEquals(CustomerTable.columns, hardwareCustomers.columns)

        assertGeneratedSql("""
            SELECT T0."id" "c0"
            , T0."shop" "c1"
            , T0."name" "c2"
            , T0."spent" "c3"
            FROM "Shop" T1
            INNER JOIN "Customer" T0 ON T1."id" = T0."shop"
            WHERE T1."id" = ?
            ORDER BY T0."name" ASC
        """)

        /*
        You can pass multiple `Table`s to select:
         */

        ShopTable
            .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
            .select(ShopTable, CustomerTable)
            .perform(db)

        assertGeneratedSql("""
            SELECT T0."id" "c0"
            , T0."name" "c1"
            , T0."address" "c2"
            , T0."established" "c3"
            , T1."id" "c4"
            , T1."shop" "c5"
            , T1."name" "c6"
            , T1."spent" "c7"
            FROM "Shop" T0
            INNER JOIN "Customer" T1 ON T0."id" = T1."shop"
        """)

        /* HIDE */

        Unit
    }

    @Test
    fun selectDistinct() = testExampleDatabase {
        /* SHOW */

        /*
        ### Select distinct
        All `.select(...)` methods have a corresponding `.selectDistinct(...)`.
         */

        val distinctShopIds = CustomerTable
            .selectDistinct(CustomerTable.shop)
            .perform(db)
            .map { it.first() }
            .toList()

        assertContentEquals(distinctShopIds, distinctShopIds.distinct())

        /* HIDE */
    }

    @Test
    fun explicitLabel() = testExampleDatabase {
        /* SHOW */

        /*
        ### Expressions and labels

        Expressions can be selected by using `as_` to label them.
        You can use an existing column name as a label or create an anonymous label:
         */

        val lowercaseName = label<String>()

        /*
        Here we select `LOWER(ShopTable.name)` in SQL and label it:
        */

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(
                ShopTable,
                lower(ShopTable.name) as_ lowercaseName
            )
            .perform(db)
            .single()

        /*
        The label can then be used to access the result of the expression:
         */

        assertEquals("helen's hardware", row[lowercaseName])
        assertEquals(row[ShopTable.name].lowercase(), row[lowercaseName])

        /* HIDE */
    }

    @Test
    fun labeledExpr() = testExampleDatabase {
        /* SHOW */

        /*
        For convenience you can use the labeled expression to refer to the label.
        Here is another way of writing the code from above:
         */

        val lowerName = lower(ShopTable.name) as_ label()

        val row = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(ShopTable, lowerName)
            .perform(db)
            .single()

        assertEquals("helen's hardware", row[lowerName])
        assertEquals(row[ShopTable.name].lowercase(), row[lowerName])

        /* HIDE */
    }

    @Test
    fun selectEmpty() = testExampleDatabase {
        /* SHOW */

        /*
        ### Empty selections

        Selects can be empty. This will generate SQL with `SELECT 1`

         */

        val emptySelect = ShopTable
            .where(ShopTable.id inValues listOf(hardwareStoreId, groceryStoreId))
            .select()
            .perform(db)

        assertEquals(0, emptySelect.columns.size)

        val rowCount = emptySelect
            .count()

        assertEquals(2, rowCount)

        assertGeneratedSql("""
            SELECT ? "c0"
            FROM "Shop" T0
            WHERE T0."id" IN (?, ?)
        """)

        /* HIDE */
    }

    @Test
    fun expectPair() = testExampleDatabase {
        /* SHOW */

        /*
        ### Expecting columns

        If you know the exact columns a query will have at runtime,
        you can convert it to a query of statically typed rows.
        This is sometimes necessary when using subqueries in expressions.
        */

        val columnsList = listOf(ShopTable.address, ShopTable.name)

        val query = ShopTable
            .where(ShopTable.id eq hardwareStoreId)
            .select(columnsList)

        val genericRow: ResultRow = query
            .perform(db)
            .single()

        val staticallyTypedRow: RowOfTwo<String, String> = query
            .expecting(ShopTable.name, ShopTable.address) // convert to statically typed query
            .perform(db)
            .single()

        assertEquals(
            genericRow[ShopTable.name],
            staticallyTypedRow.first()
        )

        /*

        This will not work if the columns sets do not match at runtime. The following code will fail:

         */

        assertFails {
            ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(listOf(ShopTable.address, ShopTable.name))
                .expecting(ShopTable.name)
        }

        /* HIDE */

        Unit
    }
}

/* SHOW */
/* ## Joins */
/* HIDE */

class Joins {
    @Test
    fun innerJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Inner join
         */

        CustomerTable
            .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun leftJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Left and right join
         */

        CustomerTable
            .leftJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        CustomerTable
            .rightJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun crossJoin() = testExampleDatabase {
        /* SHOW */
        /*
        ### Cross join
         */

        ShopTable
            .crossJoin(CustomerTable)
            .perform(db)

        /* HIDE */
    }
}

/* SHOW */
/* ## Aliases */
/* HIDE */

class Aliases {
    @Test
    fun selectAliased() = testExampleDatabase {
        /* SHOW */

        /*
        ### Self-join with alias
         */

        val alias = alias()

        val row = ShopTable
            .innerJoin(ShopTable.as_(alias), alias[ShopTable.id] eq groceryStoreId)
            .where(ShopTable.id eq hardwareStoreId)
            .perform(db)
            .single()

        assertEquals("Helen's Hardware", row[ShopTable.name])
        assertEquals("24 Hr Groceries", row[alias[ShopTable.name]])

        /* HIDE */
    }
}

/* SHOW */
/* ## Where */
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
            .where(ShopTable.name eq "Helen's Hardware")
            .perform(db)
            .single()

        /* HIDE */
    }
}

/* SHOW */
/* ## Group By */
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


/* SHOW */
/* ## Windows */
/* HIDE */

class Windows {
    @Test
    fun selectingWindows() = testExampleDatabase {
        /* SHOW */

        /*
        ### Window clause
         */

        val shopsWindow = window() as_ all().partitionBy(ShopTable.id)

        val totalSpent = sum(CustomerTable.spent).over(shopsWindow) as_ label()
        val rank = rank().over(shopsWindow.orderBy(CustomerTable.spent.desc())) as_ label()

        val rankings = ShopTable
            .innerJoin(CustomerTable, CustomerTable.shop eq ShopTable.id)
            .window(shopsWindow)
            .orderBy(ShopTable.name, rank)
            .select(
                ShopTable.name,
                CustomerTable.name,
                CustomerTable.spent,
                rank,
                totalSpent
            )
            .perform(db)
            .map { row ->
                "${row[CustomerTable.name]} #${row[rank]} at ${row[ShopTable.name]} " +
                "spent $${row[CustomerTable.spent]} of $${row[totalSpent]}"
            }
            .toList()

        assertContentEquals(
            rankings,
            listOf(
                "Angela Abara #1 at 24 Hr Groceries spent $79.99 of $79.99",
                "Michael M. Michael #1 at Helen's Hardware spent $125.00 of $145.50",
                "Maria Robinson #2 at Helen's Hardware spent $20.50 of $145.50"
            )
        )

        assertGeneratedSql("""
            SELECT T0."name" "c0"
            , T1."name" "c1"
            , T1."spent" "c2"
            , RANK() OVER (w0 ORDER BY T1."spent" DESC) "c3"
            , SUM(T1."spent") OVER (w0) "c4"
            FROM "Shop" T0
            INNER JOIN "Customer" T1 ON T1."shop" = T0."id"
            WINDOW w0 AS (PARTITION BY T0."id")
            ORDER BY T0."name" ASC, "c3" ASC
        """)

        /* HIDE */
    }
}

/* SHOW */
/* ## Values */
/* HIDE */

class Values {
}

/* SHOW */
/* ## Unions */
/* HIDE */

class Unions {
}


/* SHOW */
/* ## Order By */
/* HIDE */

class OrderBy {
    @Test
    fun orderBy() = testExampleDatabase {
        /* SHOW */
        /*
        ### Order by
         */

        val alphabetical = ShopTable
            .orderBy(ShopTable.name)
            .perform(db)

        val reverseAlphabetical = ShopTable
            .orderBy(ShopTable.name.desc())
            .perform(db)

        /* HIDE */
    }

    @Test
    fun orderByDesc() = testExampleDatabase {
        /* SHOW */
        /*
        ### Nulls first and last
         */

        ShopTable
            .orderBy(ShopTable.established.desc().nullsLast())
            .perform(db)

        ShopTable
            .orderBy(ShopTable.established.asc().nullsFirst())
            .perform(db)

        /* HIDE */
    }

    @Test
    fun multiOrderBy() = testExampleDatabase {
        /* SHOW */
        /*
        ### Compound order
         */

        ShopTable
            .orderBy(
                ShopTable.established.desc().nullsLast(),
                ShopTable.name
            )
            .perform(db)

        /* HIDE */
    }
}

/* SHOW */
/* ## Limits */
/* HIDE */

class Limits {
    @Test
    fun limits() = testExampleDatabase {
        /* SHOW */
        /*
        ### Limit
         */

        val firstByName = ShopTable
            .orderBy(ShopTable.name)
            .limit(1)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun offsets() = testExampleDatabase {
        /* SHOW */
        /*
        ### Offset
         */

        val thirdByName = ShopTable
            .orderBy(ShopTable.name)
            .offset(2)
            .limit(1)
            .perform(db)

        /* HIDE */
    }

    @Test
    fun offsetWithoutLimit() = testExampleDatabase {
        /* SHOW */
        /*
        ### Offset without limit
         */

        ShopTable
            .orderBy(ShopTable.name)
            .offset(2)
            .perform(db)

        /* HIDE */
    }
}

/* SHOW */
/* ## Locking */
/* HIDE */

class Locking {
}

/* SHOW */
/* ## With */
/* HIDE */

class With {
}