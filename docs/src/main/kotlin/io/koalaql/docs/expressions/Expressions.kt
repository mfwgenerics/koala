package io.koalaql.docs.expressions

import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.execBlock
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.*
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.case() {
    h1("Case Expressions")

    h2("Case")

    -"Case expressions are created by calling `case`"

    val tabbedBlocks = TabbedBlocks()

    testExampleDatabase {
        val kotlin = execBlock {
            val shopType = case(
                ShopTable.id,
                when_(hardwareStoreId).then("HARDWARE"),
                when_(groceryStoreId).then("GROCERIES"),
                else_ = value("OTHER")
            ) as_ label()

            val type = ShopTable
                .where(ShopTable.id eq groceryStoreId)
                .select(shopType)
                .perform(db)
                .single()
                .getValue(shopType)

            check(type == "GROCERIES")
        }

        tabbedBlocks.tabs {
            kotlin(kotlin)
            sql(popGeneratedSql())
        }
    }

    h2("Empty case")

    -"The subject of a case expression can be omitted."
    -"The case expression will then match the first true condition."

    testExampleDatabase {
        tabbedBlocks.withGeneratedSql {
            val shopType = case(
                when_(ShopTable.id eq hardwareStoreId).then("HARDWARE"),
                when_(ShopTable.id eq groceryStoreId).then("GROCERIES")
            ) as_ label()

            val type = ShopTable
                .where(ShopTable.id eq groceryStoreId)
                .select(shopType)
                .perform(db)
                .single()
                .getValue(shopType)

            check(type == "GROCERIES")
        }
    }
}

fun DocusaurusMarkdownFile.strings() {
    val tabbedBlocks = TabbedBlocks()

    h1("String Operations")

    h2("Like")

    testExampleDatabase {
        tabbedBlocks.withGeneratedSql {
            val row = ShopTable
                .where(ShopTable.name like "%Hardware%")
                .where(ShopTable.name notLike "%Groceries%")
                .perform(db)
                .single()

            check(hardwareStoreId == row.getValue(ShopTable.id))
        }
    }

    h2("Upper and lower")

    testExampleDatabase {
        tabbedBlocks.withGeneratedSql {
            val (lowerName, upperName) = ShopTable
                .where(ShopTable.id eq hardwareStoreId)
                .select(
                    lower(ShopTable.name) as_ label(),
                    upper(ShopTable.name) as_ label(),
                )
                .perform(db)
                .single()

            check("helen's hardware" == lowerName)
            check("HELEN'S HARDWARE" == upperName)
        }
    }
}