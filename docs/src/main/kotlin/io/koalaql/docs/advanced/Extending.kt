package io.koalaql.docs.advanced

import io.koalaql.data.JdbcExtendedDataType
import io.koalaql.data.JdbcMappedType
import io.koalaql.ddl.ExtendedDataType
import io.koalaql.ddl.INTEGER
import io.koalaql.ddl.Table
import io.koalaql.ddl.VARCHAR
import io.koalaql.docs.TabbedBlocks
import io.koalaql.docs.testExampleDatabase
import io.koalaql.dsl.rowOf
import io.koalaql.dsl.setTo
import io.koalaql.dsl.values
import io.koalaql.kapshot.CaptureSource
import io.koalaql.kapshot.CapturedBlock
import io.koalaql.kapshot.sourceOf
import io.koalaql.markout.docusaurus.DocusaurusMarkdownFile
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

@CaptureSource
data class Email(
    val asString: String
)

@CaptureSource
object CustomerTable : Table("Emails") {
    /*
    EMAIL will be treated as a VARCHAR(256) in generated SQL.
    Here we provide mappings between Email and the base type of String.
    */
    val EMAIL = VARCHAR(256).map(
        to = { string -> Email(string) },
        from = { email -> email.asString }
    )

    /* EMAIL can be treated like any other column type. Here we use it as a primary key. */
    val email = column("email", EMAIL.primaryKey())
    val name = column("name", VARCHAR(256))
}

@CaptureSource
enum class TShirtEnum {
    XS, S, M, L, XL;
}

fun DocusaurusMarkdownFile.extending() {
    val tabbedBlocks = TabbedBlocks()

    h1("Extending the DSL")

    -"In practice, it is necessary to extend Koala to support a larger set of SQL"
    -"and to better integrate with user-defined data types. Koala provides"
    -"different mechanisms to extend the library without having to revert to"
    -"raw queries."

    h2("Mapped columns")

    -"Mapped columns are an easy way to support user defined types that can be expressed"
    -"in terms of existing column types."
    -"This is useful for supporting wrapper types, enums and serialized data."

    p {
        -"The code below establishes a user-defined `Email` data type and a corresponding `EMAIL`"
        -"column type by creating a mapping from the `VARCHAR(256)` column type."
    }

    testExampleDatabase {
        code("kotlin", "${sourceOf<Email>().text}\n\n${sourceOf<CustomerTable>().text}")

        -"Generated SQL will treat mapped column types the exact same way as their unmapped counterparts."

        db.declareTables(CustomerTable)

        code("sql", "SQL", popGeneratedSql())

        -"In the code below, we see our `Email` values become plain strings in the generated SQL."

        tabbedBlocks.withGeneratedSql {
            CustomerTable
                .insert(
                    rowOf(
                        CustomerTable.name setTo "Emanuel Smith",
                        CustomerTable.email setTo Email("e.smith@example.com")
                    )
                )
                .perform(db)
        }

        h3("Enum columns")

        -"A common use case for mapped columns is storing and working with enums as strings."
        -"Koala provides a method for easily creating enum mappings. Enum mappings can use any column"
        -"type as a base."

        code("kotlin",
            sourceOf<TShirtEnum>().text + "\n\n" + CapturedBlock {
                val TSHIRT_AS_VARCHAR = VARCHAR(256).mapToEnum<TShirtEnum> { tshirt ->
                    tshirt.name
                }

                val TSHIRT_AS_INTEGER = INTEGER.mapToEnum<TShirtEnum> { tshirt ->
                    tshirt.ordinal
                }
            }.source.text
        )

        caution {
            -"Storing enums as ints using `Enum.ordinal` can introduce backwards compatibility problems."
        }

        h2("New column types")

        -"Koala doesn't natively support all the column types that exist across different SQL dialects."
        -"We provide `${JdbcExtendedDataType::class.simpleName}` to support column types that are not"
        -"included in the library."

        p {
            -"The code below represents H2's UUID column type as a `java.util.UUID`."
        }

        code {
            val UUID_H2 = JdbcExtendedDataType(
                sql = "UUID", /* The raw SQL name of the column */
                jdbc = object : JdbcMappedType<UUID> { /* JDBC bindings for writing and reading UUIDs */
                    override fun writeJdbc(stmt: PreparedStatement, index: Int, value: UUID) {
                        stmt.setObject(index, value)
                    }

                    override fun readJdbc(rs: ResultSet, index: Int): UUID? =
                        rs.getObject(index) as? UUID /* We need to handle the NULL case */
                }
            )
        }
    }
}