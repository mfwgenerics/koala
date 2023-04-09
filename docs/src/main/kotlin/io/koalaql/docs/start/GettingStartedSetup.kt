package io.koalaql.docs.start

import io.koalaql.docs.KOALA_PUBLISHED_VERSION
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.projectSetup(
    title: String,
    suffix: String,
    exampleUrls: String,
    instantiate: Instantiation
) {
    h1(title)

    h2("Dependencies")

    code("kotlin", "build.gradle.kts", """
        dependencies {
            implementation("io.koalaql:koala-core:$KOALA_PUBLISHED_VERSION")
            implementation("io.koalaql:koala-jdbc:$KOALA_PUBLISHED_VERSION")
            implementation("io.koalaql:koala-$suffix:$KOALA_PUBLISHED_VERSION")
        }
    """.trimIndent())

    h2("Instantiating the database")

    p {
        -"Koala models your database as a `DataSource`. You should obtain one once at the start of your program."
        -"The only required argument is the provider which supplies JDBC connections."
        -"JDBC connections can be supplied directly from the driver or from a connection pool like "
        a("https://github.com/brettwooldridge/HikariCP", "HikariCP")
    }

    code("kotlin", "val ds = ${instantiate.source.text}")

    info("JDBC") {
        -"Read more " + a(exampleUrls, "here")
        -"to learn more about JDBC for $title and see example values for `jdbcUrl`."
    }
}