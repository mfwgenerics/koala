package io.koalaql.docs.writing

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

fun DocusaurusMarkdownFile.queries() {
    h1("Queries")

    h2("Selects")

    val tabbedBlocks = TabbedBlocks()

    testExampleDatabase {
        h3("Selecting all columns")

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
        h3("Individual columns")

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
        h3("All columns of a Table")

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
        h3("Select distinct")

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
        h3("Expressions and labels")

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
        h3("Empty selections")

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
        h3("Expecting columns")

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

    h2("Joins")

    testExampleDatabase {
        h3("Inner Join")

        tabbedBlocks.withGeneratedSql {
            CustomerTable
                .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Left and right join")

        tabbedBlocks.withGeneratedSql {
            CustomerTable
                .leftJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
                .perform(db)

            CustomerTable
                .rightJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Cross join")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .crossJoin(CustomerTable)
                .perform(db)
        }
    }

    h2("Aliases")

    testExampleDatabase {
        h3("Self-join with alias")

        tabbedBlocks.withGeneratedSql {
            val alias = alias()

            val row = ShopTable
                .innerJoin(ShopTable.as_(alias), alias[ShopTable.id] eq groceryStoreId)
                .where(ShopTable.id eq hardwareStoreId)
                .perform(db)
                .single()

            check("Helen's Hardware" == row[ShopTable.name])
            check("24 Hr Groceries" == row[alias[ShopTable.name]])
        }
    }

    h2("Where")

    testExampleDatabase {
        h3("Chaining wheres")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .where(ShopTable.name eq "Helen's Hardware")
                .perform(db)
                .single()
        }
    }

    h2("Group By")

    testExampleDatabase {
        h3("Group by")

        tabbedBlocks.withGeneratedSql {
            val customerCount = label<Int>()

            CustomerTable
                .groupBy(CustomerTable.shop)
                .select(CustomerTable.shop, count() as_ customerCount)
                .perform(db)
        }
    }

    h2("Windows")

    testExampleDatabase {
        h3("Window clause")

        tabbedBlocks.withGeneratedSql {
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

            check(
                rankings.deepEquals(
                    listOf(
                        "Angela Abara #1 at 24 Hr Groceries spent $79.99 of $79.99",
                        "Michael M. Michael #1 at Helen's Hardware spent $125.00 of $145.50",
                        "Maria Robinson #2 at Helen's Hardware spent $20.50 of $145.50"
                    )
                )
            )
        }
    }

    h2("Values")

    testExampleDatabase {
        h3("From rows")

        tabbedBlocks.withGeneratedSql {
            val fakeShop = rowOf(
                ShopTable.name setTo "Fake Shop",
                ShopTable.address setTo "79 Fake Street, Fakesville"
            )

            val result = values(fakeShop)
                .perform(db)
                .single()

            check("Fake Shop" == result[ShopTable.name])
        }
    }

    testExampleDatabase {
        h3("From a collection")

        tabbedBlocks.withGeneratedSql {
            val names = listOf("Dylan", "Santiago", "Chloe")

            val customers = values(names) { name ->
                this[CustomerTable.name] = name
                this[CustomerTable.shop] = groceryStoreId
                this[CustomerTable.spent] = BigDecimal("10.80")
            }

            CustomerTable
                .insert(customers)
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Using labels")

        tabbedBlocks.withGeneratedSql {

            val name = label<String>("name")
            val age = label<Int>("age")

            val names = values(listOf("Jeremy", "Sofia")) {
                this[name] = it
                this[age] = 29
            }

            val (firstName, firstAge) = names
                .subquery()
                .orderBy(name.desc())
                .select(upper(name) as_ name, age)
                .perform(db)
                .first()

            check(firstName == "SOFIA")
            check(firstAge == 29)
        }
    }

    h2("Unions")

    h2("Order By")

    testExampleDatabase {
        h3("Order by")

        tabbedBlocks.withGeneratedSql {
            val alphabetical = ShopTable
                .orderBy(ShopTable.name)
                .perform(db)

            val reverseAlphabetical = ShopTable
                .orderBy(ShopTable.name.desc())
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Nulls first and last")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .orderBy(ShopTable.established.desc().nullsLast())
                .perform(db)

            ShopTable
                .orderBy(ShopTable.established.asc().nullsFirst())
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Compound order")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .orderBy(
                    ShopTable.established.desc().nullsLast(),
                    ShopTable.name
                )
                .perform(db)
        }
    }

    h2("Limits")

    testExampleDatabase {
        h3("Limit")

        tabbedBlocks.withGeneratedSql {
            val firstByName = ShopTable
                .orderBy(ShopTable.name)
                .limit(1)
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Offset")

        tabbedBlocks.withGeneratedSql {
            val thirdByName = ShopTable
                .orderBy(ShopTable.name)
                .offset(2)
                .limit(1)
                .perform(db)
        }
    }

    testExampleDatabase {
        h3("Offset without limit")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .orderBy(ShopTable.name)
                .offset(2)
                .perform(db)
        }
    }

    h2("Locking")

    testExampleDatabase {
        h3("For update")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .where(ShopTable.id.inValues(groceryStoreId, hardwareStoreId))
                .forUpdate()
                .perform(db)
        }
    }

    h2("With")
}