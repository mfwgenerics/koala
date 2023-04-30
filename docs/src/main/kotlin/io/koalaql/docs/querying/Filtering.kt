package io.koalaql.docs.querying

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.math.BigDecimal

fun DocusaurusMarkdownFile.where() {
    val tabbedBlocks = TabbedBlocks()

    h1("Where clauses")

    testExampleDatabase {
        h2("Chaining wheres")

        tabbedBlocks.withGeneratedSql {
            ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .where(ShopTable.name eq "Helen's Hardware")
                .perform(db)
                .single()
        }
    }
}