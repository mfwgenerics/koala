import io.koalaql.docs.advanced.extending
import io.koalaql.docs.examples.quickExample
import io.koalaql.docs.executing.resultSets
import io.koalaql.docs.expressions.case
import io.koalaql.docs.querying.*
import io.koalaql.docs.start.projectSetup
import io.koalaql.docs.updating.updates
import io.koalaql.docs.expressions.strings
import io.koalaql.docs.introduction.overview
import io.koalaql.docs.introduction.whyKoala
import io.koalaql.docs.usage.usage
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

                metadata = mapOf("google-site-verification" to "E-XuQoF0UqA8bzoXL3yY7bs9KuQFsQ2yrSkYuIp6Gqs")
            }

            docs {
                directory("introduction") {
                    label = "Introduction"

                    markdown("overview") { overview() }
                    markdown("why") { whyKoala() }
                }

                directory("getting-started") {
                    label = "Getting started"

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

                directory("usage") {
                    label = "General usage"

                    usage()
                }

                directory("queries") {
                    label = "Queries"

                    markdown("selecting") { selects() }
                    markdown("results") { resultSets() }
                    markdown("joining") { joins() }
                    markdown("filtering") { where() }
                    markdown("grouping") { grouping() }
                    markdown("ordering") { ordering() }
                    markdown("limits") { limits() }
                    markdown("windows") { windows() }
                    markdown("values") { values() }
                    markdown("locks") { locks() }
                }

                directory("statements") {
                    label = "Statements"

                    markdown("updates") { updates() }
                }

                directory("functions-and-expressions") {
                    label = "Functions and operations"

                    markdown("case") { case() }
                    markdown("strings") { strings() }
                }

                directory("advanced-usage") {
                    label = "Advanced usage"

                    markdown("extending") { extending() }
                }
            }
        }
    }
}