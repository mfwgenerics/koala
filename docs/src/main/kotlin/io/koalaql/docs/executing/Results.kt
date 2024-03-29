package io.koalaql.docs.executing

import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.execBlock
import io.koalaql.docs.tables.ShopTable
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import io.koalaql.transact
import io.koalaql.values.ResultRow

fun DocusaurusMarkdownFile.resultSets() {
    h1("Result sets")

    val db = ExampleDatabase().db

    db.transact { cxn ->
        h3("Working with results")

        -"SQL queries return sequences of result rows."
        -"Iterating through the sequence will fetch results from the underlying JDBC ResultSet"

        code("kotlin", execBlock {
            val names = ShopTable
                .selectAll()
                .perform(cxn)
                .map { row -> // this is Kotlin's Sequence.map
                    row[ShopTable.name]
                }
                .toSet()

            check("Helen's Hardware" in names)
            check("24 Hr Groceries" in names)
        })

        info {
            -"Once a result sequence has been fully iterated the underlying JDBC resources are closed."
            -"Unclosed resources will remain open until the connection is closed at the end of the transaction."
        }

        h3("Indexing into rows")

        p {
            -"Directly indexing into a row by a non-nullable column will always expect a non-null value."
            -"Indexing by anything other than a non-nullable column will return a nullable value."
        }

        -"Rows support `getValue` and `getOrNull` operations for explicitly dealing with null values:"

        code("kotlin", execBlock {
            val row: ResultRow = ShopTable
                .selectAll()
                .perform(cxn)
                .first()

            val nonNull: String = row.getValue(ShopTable.name) // always expect non-null
            val nullable: String? = row.getOrNull(ShopTable.name) // never expect non-null

            row[ShopTable.name] // returns a non-null value
            row[ShopTable.established] // returns a nullable value

            check(row[ShopTable.name] == row.getValue(ShopTable.name))
        })

        caution {
            -"Some queries may return null values for non-nullable columns."
            -"A common cause of this is selecting from a left joined table."
            -"In these circumstances, use `getOrNull` explicitly to retrieve the column values."
        }
    }
}