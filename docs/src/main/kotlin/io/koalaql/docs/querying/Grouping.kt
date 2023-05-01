package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.math.BigDecimal

fun DocusaurusMarkdownFile.grouping() {
    val tabbedBlocks = TabbedBlocks()

    h1("Grouping")

    testExampleDatabase {
        h2("Group by")

        tabbedBlocks.withGeneratedSql {
            val customerCount = label<Int>()

            CustomerTable
                .groupBy(CustomerTable.shop)
                .select(CustomerTable.shop, count() as_ customerCount)
                .perform(db)
        }
    }
}