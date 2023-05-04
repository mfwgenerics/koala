package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.math.BigDecimal

fun DocusaurusMarkdownFile.limits() {
    val tabbedBlocks = TabbedBlocks()

    h1("Limit and offset")

    testExampleDatabase {
        h2("Limit")

        tabbedBlocks.withGeneratedSql {
            val firstByName = ShopTable
                .orderBy(ShopTable.name)
                .limit(1)
                .perform(db)
        }
    }

    testExampleDatabase {
        h2("Offset")

        tabbedBlocks.withGeneratedSql {
            val thirdByName = ShopTable
                .orderBy(ShopTable.name)
                .offset(2)
                .limit(1)
                .perform(db)
        }
    }

    testExampleDatabase {
        h2("Offset without limit")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .orderBy(ShopTable.name)
                .offset(2)
                .perform(db)
        }
    }
}