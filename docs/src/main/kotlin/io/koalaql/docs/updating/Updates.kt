package io.koalaql.docs.updating

import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.execBlock
import io.koalaql.docs.tables.ShopTable
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.updates() {
    h1("Update")

    h2("Empty Updates")

    val db = ExampleDatabase().db

    code("kotlin", execBlock {
        val updated = ShopTable
            .update()
            .perform(db)

        check(0 == updated)
    })
}