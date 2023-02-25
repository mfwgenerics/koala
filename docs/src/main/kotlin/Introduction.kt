import io.koalaql.markout.docusaurus.Docusaurus

fun Docusaurus.introduction() = markdown("intro") {
    slug = "/"

    h1("Introduction")

    -"Koala is a Kotlin JVM library for building and executing SQL."
    -"It is designed to be a more powerful and complete alternative "
    -"to the SQL DSL layer in ORMs like "+a("https://github.com/kotlin-orm/ktorm", "Ktorm")
    -"and "+a("https://github.com/JetBrains/Exposed", "Exposed")+"."
    -"Koala is not an ORM and does not perform any Entity mapping."
    -"Koala provides:"

    ul {
        li("Discoverable syntax that takes advantage of IDE auto-complete.")
        li("Native support for advanced SQL like `WINDOW`, `WITH`, self-joins and more.")
        li("An immutable and referentially transparent DSL.")
        li("Predictable generation and execution of SQL. No N+1 or hidden laziness.")
        li {
            -"Automatic schema migration for prototyping similar to"
            -""+a("https://www.prisma.io/", "Prisma")+"'s `db push`."
        }
        li("Event interfaces for user provided logging. No hardcoded logging.")
        li("Pure Kotlin dependencies with no code generation or plugins required.")
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