package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.kapshot.CapturedBlock
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import io.koalaql.values.ResultRow
import io.koalaql.values.RowOfTwo
import java.math.BigDecimal

infix fun <T> Collection<T>.deepEquals(other: Collection<T>): Boolean {
    if (size != other.size) return false

    return asSequence()
        .zip(other.asSequence())
        .all { (x, y) -> x == y }
}

fun DocusaurusMarkdownFile.selects() {
    h1("Selects")

    val tabbedBlocks = TabbedBlocks()

    testExampleDatabase {
        h2("Selecting all columns")

        -"Use `.selectAll()` to select all columns from a query."

        tabbedBlocks.withGeneratedSql {
            val allSelected = ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .selectAll()
                .perform(db)

            check(ShopTable.columns.deepEquals(allSelected.columns))
            check("Helen's Hardware" == allSelected.first()[ShopTable.name])
        }

        -"In most cases `.selectAll()` can be omitted:"

        tabbedBlocks.withGeneratedSql {
            val implicitSelectAll = ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .perform(db)

            check(ShopTable.columns.deepEquals(implicitSelectAll.columns))
        }

        info {
            -"`SELECT * FROM` will never appear in generated SQL."
            -"The generated SQL for these queries will name columns explicitly."
        }
    }

    testExampleDatabase {
        h2("Individual columns")

        -"Selecting a small number of fixed columns gives you a specialized"
        -"query with statically typed rows of ordered columns."
        -"You can use Kotlin's destructuring or call positional"
        -"methods to access these fields in a type safe way."

        tabbedBlocks.withGeneratedSql {
            val row: RowOfTwo<String, String> = ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(ShopTable.name, ShopTable.address) // select a pair
                .perform(db)
                .single()

            val (name, address) = row // destructuring support

            // using positional methods also works
            check(name == row.first())

            check(row[ShopTable.name] == name)
            check(row[ShopTable.address] == address)

            check("Helen's Hardware @ 63 Smith Street, Caledonia, 62281D" == "$name @ $address")
        }
    }

    testExampleDatabase {
        h2("All columns of a Table")

        -"Passing a `Table` to `.select` will automatically select all fields from that `Table`:"

        tabbedBlocks.withGeneratedSql {
            val hardwareCustomers = ShopTable
                .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
                .where(ShopTable.id eq hardwareStoreId)
                .orderBy(CustomerTable.name)
                .select(CustomerTable) // select only fields from CustomerTable
                .perform(db)

            check(CustomerTable.columns.deepEquals(hardwareCustomers.columns))
        }

        -"You can pass multiple `Table`s to select:"

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
                .select(ShopTable, CustomerTable)
                .perform(db)
        }
    }

    testExampleDatabase {
        h2("Select distinct")

        -"All `.select(...)` methods have a corresponding `.selectDistinct(...)`."

        tabbedBlocks.withGeneratedSql {
            val distinctShopIds = CustomerTable
                .selectDistinct(CustomerTable.shop)
                .perform(db)
                .map { it.first() }
                .toList()

            check(distinctShopIds.deepEquals(distinctShopIds.distinct()))
        }
    }

    testExampleDatabase {
        h2("Expressions and labels")

        -"Expressions can be selected by using `as_` to label them."
        -"You can use an existing column name as a label or create an anonymous label:"

        tabbedBlocks.withGeneratedSql {
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

            check("helen's hardware" == row[lowercaseName])
            check(row[ShopTable.name].lowercase() == row[lowercaseName])
        }
    }

    testExampleDatabase {
        -"For convenience you can use the labeled expression to refer to the label."
        -"Here is another way of writing the code from above:"

        tabbedBlocks.withGeneratedSql {
            val lowerName = lower(ShopTable.name) as_ label()

            val row = ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(ShopTable, lowerName)
                .perform(db)
                .single()

            check("helen's hardware" == row[lowerName])
            check(row[ShopTable.name].lowercase() == row[lowerName])
        }
    }

    testExampleDatabase {
        h2("Empty selections")

        -"Selects can be empty. This will generate SQL with `SELECT 1`"

        tabbedBlocks.withGeneratedSql {
            val emptySelect = ShopTable
                .where(ShopTable.id inValues listOf(hardwareStoreId, groceryStoreId))
                .select()
                .perform(db)

            check(emptySelect.columns.isEmpty())

            val rowCount = emptySelect
                .count()

            check(2 == rowCount)
        }
    }

    testExampleDatabase {
        h2("Expecting columns")

        -"If you know the exact columns a query will have at runtime,"
        -"you can convert it to a query of statically typed rows."
        -"This is sometimes necessary when using subqueries in expressions."

        tabbedBlocks.withGeneratedSql {
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

            check(genericRow[ShopTable.name] == staticallyTypedRow.first())
        }

        -"This will not work if the columns sets do not match at runtime. The following code will fail:"

        val failingCode = CapturedBlock {
            ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(listOf(ShopTable.address, ShopTable.name))
                .expecting(ShopTable.name)
        }

        try {
            failingCode()
            check(false)
        } catch (ignored: IllegalStateException) { }

        code("kotlin", failingCode.source.text)
    }
}