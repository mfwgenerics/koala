package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.locks() {
    val tabbedBlocks = TabbedBlocks()

    h1("Locks")

    testExampleDatabase {
        h2("For update")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .where(ShopTable.id.inValues(groceryStoreId, hardwareStoreId))
                .forUpdate()
                .perform(db)
        }
    }

    h1("With")
}