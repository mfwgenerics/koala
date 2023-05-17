package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.math.BigDecimal

fun DocusaurusMarkdownFile.ordering() {
    val tabbedBlocks = TabbedBlocks()

    h1("Ordering")

    testExampleDatabase {
        h2("Order by")

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
        h2("Nulls first and last")

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
        h2("Compound order")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .orderBy(
                    ShopTable.established.desc().nullsLast(),
                    ShopTable.name
                )
                .perform(db)
        }
    }
}