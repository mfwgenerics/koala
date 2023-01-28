import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.examples.quickExample
import io.koalaql.docs.execBlock
import io.koalaql.docs.executing.resultSets
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.writing.expressions
import io.koalaql.docs.writing.queries
import io.koalaql.markout.docusaurus.docusaurus
import io.koalaql.markout.markout
import kotlin.io.path.Path

fun main() = markout(Path("site/docs")) {
    docusaurus {
        markdown("intro") {
            slug = "/"

            h1("Introduction")
        }

        directory("about") {
            label = "About Koala"

            markdown("placeholder") {
                h1("Under Construction")
            }
        }

        directory("schema") {
            label = "Declaring Schema"

            markdown("placeholder") {
                h1("Under Construction")
            }
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

        directory("examples") {
            label = "Examples"

            markdown("quick") { quickExample() }
        }
    }
}