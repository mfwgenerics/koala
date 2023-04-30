package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.math.BigDecimal

fun DocusaurusMarkdownFile.values() {
    val tabbedBlocks = TabbedBlocks()

    h1("Values clauses")

    testExampleDatabase {
        h2("From rows")

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
        h2("From a collection")

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
        h2("Using labels")

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
}