package io.koalaql.docs.usage

import io.koalaql.DataSource
import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.execBlock
import io.koalaql.docs.tables.CustomerTable
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.eq
import io.koalaql.h2.generateH2Sql
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import io.koalaql.mysql.generateMysqlSql
import io.koalaql.postgres.generatePostgresSql
import io.koalaql.query.SqlPerformer
import io.koalaql.query.fluent.PerformableSql
import io.koalaql.sql.CompiledSql

fun DocusaurusMarkdownFile.debugging() {
    h1("Debugging")

    h2("Viewing generated SQL")

    -"For each supported database there is a method to directly generate SQL in that dialect"
    -"for the purpose of inspection."
    -"These methods can be used without instantiating a `${DataSource::class.simpleName}`."
    -"This makes them perfect for locally inspecting generated SQL in your IDE or in unit tests."

    table {
        th {
            td("Dialect")
            td("Method")
            td("Import")
        }
        tr {
            td("MySQL")
            td {
                c("${PerformableSql::class.simpleName}::${PerformableSql::generateMysqlSql.name}")
            }
            td {
                c("import io.koalaql.mysql.${PerformableSql::generateMysqlSql.name}")
            }
        }
        tr {
            td("Postgres")
            td {
                c("${PerformableSql::class.simpleName}::${PerformableSql::generatePostgresSql.name}")
            }
            td {
                c("import io.koalaql.postgres.${PerformableSql::generatePostgresSql.name}")
            }
        }
        tr {
            td("H2")
            td {
                c("${PerformableSql::class.simpleName}::${PerformableSql::generateH2Sql.name}")
            }
            td {
                c("import io.koalaql.h2.${PerformableSql::generateH2Sql.name}")
            }
        }
    }

    lateinit var printedText: String

    fun println(any: Any?) {
        printedText = "$any"
    }

    val tabbedBlocks = TabbedBlocks()

    h3("Example")

    -"Here is an example of using `${PerformableSql::generatePostgresSql.name}`"
    -"to print some generated SQL"

    tabbedBlocks.tabs {
        tab("Code", "kotlin", execBlock {
            val generated: CompiledSql? = CustomerTable
                .where(CustomerTable.id eq 123)
                .generatePostgresSql()

            println(generated?.parameterizedSql)
        })

        tab("Output", "sql", printedText)
    }

    note {
        -"The return type is nullable because certain Koala statements may be no-ops."
        -"An example is attempting to insert with an empty list of values."
    }

    h3("Using a ${DataSource::class.simpleName}")

    -"The methods above are handy if you want to quickly see the generated SQL for a dialect."
    -"If you want to see the exact SQL generated by a `${DataSource::class.simpleName}` at runtime,"
    -"you can use the `${SqlPerformer::generateSql.name}` method."

    testExampleDatabase {
        tabbedBlocks.tabs {
            tab("Code", "kotlin", execBlock {
                val generated: CompiledSql? = CustomerTable
                    .where(CustomerTable.id eq 123)
                    .generateSql(db) /* pass our DataSource */

                println(generated?.parameterizedSql)
            })

            tab("Output", "sql", printedText)
        }
    }
}