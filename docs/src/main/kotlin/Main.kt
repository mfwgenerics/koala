import io.koalaql.docs.examples.quickExample
import io.koalaql.docs.executing.resultSets
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
        }

        directory("writing") {
            label = "Writing SQL"
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