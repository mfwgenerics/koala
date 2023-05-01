package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.joins() {
    val tabbedBlocks = TabbedBlocks()

    h1("Joins")

    testExampleDatabase {
        h2("Inner Join")

        tabbedBlocks.withGeneratedSql {
            CustomerTable
                .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
                .perform(db)
        }
    }

    testExampleDatabase {
        h2("Left and right join")

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
        h2("Cross join")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .crossJoin(CustomerTable)
                .perform(db)
        }
    }

    testExampleDatabase {
        h2("Self-join with alias")

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
}