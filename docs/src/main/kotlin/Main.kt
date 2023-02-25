import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.examples.quickExample
import io.koalaql.docs.execBlock
import io.koalaql.docs.executing.resultSets
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.writing.expressions
import io.koalaql.docs.writing.queries
import io.koalaql.markout.docusaurus.docusaurus
import io.koalaql.markout.markout

fun main() = markout {
    directory("docs") {
        docusaurus("site") {
            configure {
                title = "Koala Docs"
                url = "https://mfwgenerics.github.io/"
                baseUrl = "/koala/"
                github = "https://github.com/mfwgenerics/koala"
            }

            docs {
                introduction()

                directory("getting-started") {
                    label = "Getting started"

                    markdown("quick") { quickExample() }
                }

                directory("writing") {
                    label = "Writing SQL"

                    markdown("Queries") { queries() }

                    directory("statements") {
                        label = "Statements"

                        markdown("update") {
                            h1("Updates")

                            h3("Empty Updates")

                            val db = ExampleDatabase().db

                            code("kotlin", execBlock {
                                val updated = ShopTable
                                    .update()
                                    .perform(db)

                                check(0 == updated)
                            })
                        }
                    }

                    markdown("expressions") { expressions() }
                }

                directory("executing") {
                    label = "Executing SQL"

                    markdown("results") { resultSets() }
                }
            }
        }
    }
}