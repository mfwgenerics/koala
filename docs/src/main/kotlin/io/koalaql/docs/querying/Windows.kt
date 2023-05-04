package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.windows() {
    val tabbedBlocks = TabbedBlocks()

    h1("Windows")

    testExampleDatabase {
        h2("Window clause")

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
}