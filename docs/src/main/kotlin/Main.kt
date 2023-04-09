import io.koalaql.docs.ExampleDatabase
import io.koalaql.docs.examples.quickExample
import io.koalaql.docs.execBlock
import io.koalaql.docs.executing.resultSets
import io.koalaql.docs.start.projectSetup
import io.koalaql.docs.tables.ShopTable
import io.koalaql.docs.writing.expressions
import io.koalaql.docs.writing.queries
import io.koalaql.h2.H2DataSource
import io.koalaql.markout.docusaurus.docusaurus
import io.koalaql.markout.markout
import io.koalaql.mysql.MysqlDataSource
import io.koalaql.postgres.PostgresDataSource
import java.sql.DriverManager

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

                    directory("databases") {
                        label = "Databases"

                        markdown("mysql") {
                            projectSetup(
                                "MySQL",
                                "mysql",
                                "https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html"
                            ) {
                                MysqlDataSource(
                                    provider = { DriverManager.getConnection(jdbcUrl) }
                                )
                            }
                        }

                        markdown("postgres") {
                            projectSetup(
                                "PostgreSQL",
                                "postgres",
                                "https://jdbc.postgresql.org/documentation/use/"
                            ) {
                                PostgresDataSource(
                                    provider = { DriverManager.getConnection(jdbcUrl) }
                                )
                            }
                        }

                        markdown("h2") {
                            projectSetup(
                                "H2",
                                "h2",
                                "http://www.h2database.com/html/features.html#database_url"
                            ) {
                                H2DataSource(
                                    provider = { DriverManager.getConnection(jdbcUrl) }
                                )
                            }
                        }
                    }

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