package io.koalaql.docs.introduction

import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile

fun DocusaurusMarkdownFile.overview() {
    slug = "/"

    h1("Overview")

    -"Koala is a Kotlin JVM library for building and executing SQL."
    -"It is designed to be a more powerful and complete alternative "
    -"to the SQL DSL layer in ORMs like "+a("https://github.com/kotlin-orm/ktorm", "Ktorm")
    -"and "+a("https://github.com/JetBrains/Exposed", "Exposed")+"."
    -"Koala is not an ORM and does not perform any Entity mapping."

    p {
        -"You can learn more about Koala in our Slack channel "
        a("https://kotlinlang.slack.com/archives/C04PT610JRK", "#koalaql")
        -"on the Kotlin Slack."
    }

    h2("Purpose")

    p {
        -"Koala is intended for teams who don't need a full ORM and require more powerful"
        -"SQL generation than what Kotlin ORMs can currently provide."
        -"Koala is designed to be self-contained, flexible and easy to integrate into existing projects."
        -"By design it does not manage its own threading, connection pooling or logging"
        -"and delegates these concerns to the user."
    }

    h2("Supported databases")

    -"Koala currently supports the following databases:"

    table {
        th {
            td("Database")
            td("SQL DSL")
            td("Migration Support")
        }

        tr {
            td("MySQL")
            td("\uD83D\uDFE9 Yes")
            td("\uD83D\uDFE9 Yes")
        }

        tr {
            td("PostgreSQL")
            td("\uD83D\uDFE9 Yes")
            td("\uD83D\uDFE8 Partial")
        }

        tr {
            td("H2")
            td("\uD83D\uDFE9 Yes")
            td("\uD83D\uDFE5 No")
        }
    }
}